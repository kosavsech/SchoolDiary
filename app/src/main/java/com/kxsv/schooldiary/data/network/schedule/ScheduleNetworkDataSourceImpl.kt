package com.kxsv.schooldiary.data.network.schedule

import android.util.Log
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.domain.AppSettingsRepository
import com.kxsv.schooldiary.domain.ScheduleNetworkDataSource
import com.kxsv.schooldiary.util.NetworkException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val TAG = "ScheduleNetworkDataSour"
private const val AUTH_COOKIE = "DNSID"
private const val BASE_URL = "https://edu.tatar.ru"

class ScheduleNetworkDataSourceImpl @Inject constructor(
	private val appSettingsRepository: AppSettingsRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ScheduleNetworkDataSource {
	// A mutex is used to ensure that reads and writes are thread-safe.
	private val accessMutex = Mutex()
	
	private fun handleErrorResponse(result: String) {
		if (result.contains(
				"Неверный логин или пароль",
				true
			)
		) throw NetworkException.IncorrectAuthDataException
		else if (result.contains(
				"Пользователь не найден",
				true
			)
		) throw NetworkException.NotLoggedInException
		else if (result.contains(
				"Доступ в систему временно заблокирован",
				true
			)
		) throw NetworkException.AccessTemporarilyBlockedException
		else if (result.contains(
				"Введите логин и пароль",
				true
			)
		) throw NetworkException.BlankInputException
	}
	
	/**
	 * Edu tatar auth
	 *
	 * @throws NetworkException.IncorrectAuthDataException
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.AccessTemporarilyBlockedException
	 * @throws NetworkException.BlankInputException
	 */
	override suspend fun eduTatarAuth(login: String, password: String) {
		Log.d(TAG, "eduTatarAuth() called login $login, password $password")
		val response = withContext(ioDispatcher) {
			Jsoup.connect("$BASE_URL/logon")
				.method(Connection.Method.POST)
				.data(mapOf(Pair("main_login2", login), Pair("main_password2", password)))
				.referrer("$BASE_URL/login")
				.execute()
		}
		val result =
			response.parse().select("body > div:nth-child(1) > div.alert.alert-danger").text()
		handleErrorResponse(result)
		appSettingsRepository.setAuthCookie(response.cookie(AUTH_COOKIE))
		appSettingsRepository.setEduLogin(login)
		appSettingsRepository.setEduPassword(password)
	}
	
	
	/**
	 * Load schedule for date
	 *
	 * @param date
	 * @return List<[NetworkSchedule]>
	 */
	override suspend fun loadScheduleForDate(date: LocalDate): List<NetworkSchedule> {
		val schedule = mutableListOf<NetworkSchedule>()
		val dayPage = getDayPage(date)
		val lessons = dayPage.select("div.d-table > table > tbody > tr")
		val lessonsAmount = lessons.size
		for (i in 0 until lessonsAmount) {
			val subjectAncestorName =
				lessons.select("tr:nth-child(${i + 1}) > td:nth-child(2)").text()
			schedule.add(
				NetworkSchedule(
					index = i,
					date = date,
					subjectAncestorName = subjectAncestorName
				)
			)
		}
		return schedule
	}
	
	/**
	 * Get edu.tatar.ru page with [targetSegment] directory. If cookie is null or expired,
	 * try to authenticate and re-launch operation.
	 *
	 * @param targetSegment
	 * @return [not parsed document of page][Document]
	 * @throws NetworkException.NotLoggedInException if cookie is not valid
	 */
	private suspend fun getPage(targetSegment: String): Document {
		Log.d(TAG, "getPage() called with: targetSegment = $targetSegment")
		// TODO: add exception on eduLogin or eduPassword is null or empty, show dialog
		//  where user can re-enter auth data
		return try {
			val cookie =
				appSettingsRepository.getAuthCookie() ?: throw NetworkException.NotLoggedInException
			val doc = withContext(ioDispatcher) {
				Jsoup.connect("$BASE_URL$targetSegment").cookie(AUTH_COOKIE, cookie).get()
			}
			if (doc.location().contains("login")) throw NetworkException.NotLoggedInException
			doc
		} catch (e: NetworkException) {
			eduTatarAuth(
				appSettingsRepository.getEduLogin()!!,
				appSettingsRepository.getEduPassword()!!
			)
			getPage(targetSegment)
		}
		
	}
	
	private suspend fun getDayPage(localDate: LocalDate): Document {
		val dateFormat = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
		return getPage("/user/diary/day?for=$dateFormat")
	}
}