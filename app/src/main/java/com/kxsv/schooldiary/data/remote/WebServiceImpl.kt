package com.kxsv.schooldiary.data.remote

import android.util.Log
import com.kxsv.schooldiary.data.remote.utils.RemoteUtils.handleErrorResponse
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.util.remote.NetworkException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val TAG = "WebServiceImpl"

class WebServiceImpl @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WebService {
	
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
		userPreferencesRepository.setAuthCookie(response.cookie(AUTH_COOKIE))
		userPreferencesRepository.setEduLogin(login)
		userPreferencesRepository.setEduPassword(password)
	}
	
	/**
	 * Get schedule from [day page][getDayPage]
	 *
	 * @param localDate
	 * @return
	 */
	override suspend fun getScheduleForDate(localDate: LocalDate): Elements {
		val dayPage = getDayPage(localDate)
		return dayPage.select("div.d-table > table > tbody > tr")
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
				userPreferencesRepository.getAuthCookie()
					?: throw NetworkException.NotLoggedInException
			val doc = withContext(ioDispatcher) {
				Jsoup.connect("$BASE_URL$targetSegment").cookie(AUTH_COOKIE, cookie).get()
			}
			Log.d(TAG, doc.location())
			if (doc.location().contains("login") or doc.location().contains("message")) {
				throw NetworkException.NotLoggedInException
			}
			doc
		} catch (e: NetworkException) {
			eduTatarAuth(
				userPreferencesRepository.getEduLogin()!!,
				userPreferencesRepository.getEduPassword()!!
			)
			getPage(targetSegment)
		}
		
	}
	
	/**
	 * Get day schedule page
	 *
	 * @param localDate
	 * @return
	 */
	private suspend fun getDayPage(localDate: LocalDate): Document {
		val dateFormat = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
		return getPage("/user/diary/day?for=$dateFormat")
	}
	
	
	companion object {
		const val AUTH_COOKIE = "DNSID"
		const val BASE_URL = "https://edu.tatar.ru"
	}
}