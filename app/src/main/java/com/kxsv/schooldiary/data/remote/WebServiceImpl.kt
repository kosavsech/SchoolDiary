package com.kxsv.schooldiary.data.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.kxsv.schooldiary.data.remote.dtos.UpdateDto
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.remote.util.RemoteUtils.handleErrorResponse
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.UnsupportedMimeTypeException
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val TAG = "WebServiceImpl"

class WebServiceImpl @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : WebService {
	
	override suspend fun getLatestAppVersion(): UpdateDto? {
		return try {
			val data = withContext(ioDispatcher) {
				Jsoup.connect(UPDATE_CHANGELOG_URL)
					.ignoreContentType(true)
					.execute()
					.body()
			}
			
			val listType = object : TypeToken<List<UpdateDto>>() {}.type
			val content = Gson().fromJson<List<UpdateDto>>(data, listType)
			content.firstOrNull()
		} catch (e: JsonSyntaxException) {
			Log.e(TAG, "getLatestAppVersion: exception", e)
			null
		} catch (e: java.net.MalformedURLException) {
			Log.e(TAG, "getLatestAppVersion: exception", e)
			null
		} catch (e: HttpStatusException) {
			Log.e(TAG, "getLatestAppVersion: exception", e)
			null
		} catch (e: UnsupportedMimeTypeException) {
			Log.e(TAG, "getLatestAppVersion: exception", e)
			null
		} catch (e: java.net.SocketTimeoutException) {
			Log.e(TAG, "getLatestAppVersion: exception", e)
			null
		} catch (e: IOException) {
			Log.e(TAG, "getLatestAppVersion: exception", e)
			null
		} catch (e: Exception) {
			Log.e(TAG, "getLatestAppVersion: exception", e)
			null
		}
	}
	
	/**
	 * Edu tatar auth
	 *
	 * @throws NetworkException.IncorrectAuthDataException
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.AccessTemporarilyBlockedException
	 * @throws NetworkException.BlankInputException
	 * @throws NetworkException.PageNotFound
	 * @throws IOException if couldn't parse document
	 */
	override suspend fun eduTatarAuth(login: String, password: String) {
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
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws IOException if couldn't parse document
	 */
	override suspend fun getDayInfo(localDate: LocalDate): Elements {
		val dayPage = getDayPage(localDate)
		return dayPage.select("div.d-table > table > tbody > tr")
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws IOException if couldn't parse document
	 */
	override suspend fun getTermEduPerformance(term: EduPerformancePeriod): Elements {
		val termPage = getTermPage(term)
		return termPage.select("table > tbody > tr")
	}
	
	/**
	 * Get edu.tatar.ru page with [targetSegment] directory.
	 *
	 * If cookie is null or expired, try to authenticate and re-launch operation.
	 *
	 * @param targetSegment
	 * @return [not parsed document of page][Document]
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws IOException if couldn't parse document
	 */
	private suspend fun getPage(targetSegment: String): Document {
		Log.d(TAG, "getPage(targetSegment = $targetSegment) is called")
		// TODO: add exception on eduLogin or eduPassword is null or empty, show dialog
		//  where user can re-enter auth data
		val login = userPreferencesRepository.getEduLogin()
		val password = userPreferencesRepository.getEduPassword()
		if (login.isNullOrBlank() || password.isNullOrBlank()) {
			throw NetworkException.NotLoggedInException
		}
		return try {
			val doc = withContext(ioDispatcher) {
				val cookie = userPreferencesRepository.getAuthCookie()
					?: throw NetworkException.NotActualAuthSessionException
				
				Jsoup.connect("$BASE_URL$targetSegment")
					.cookie(AUTH_COOKIE, cookie)
					.get()
			}
			if (doc.location().contains("login") || doc.location().contains("message")) {
				throw NetworkException.NotActualAuthSessionException
			} else if (!doc.location().contains(targetSegment)) {
				throw NetworkException.PageNotFound(message = "current location = ${doc.location()}\ntarget segment = $targetSegment")
			}
			Log.d(
				TAG,
				"getPage(targetSegment = $targetSegment) returned: doc of URL ${doc.location()}"
			)
			doc
		} catch (e: NetworkException.NotActualAuthSessionException) {
			eduTatarAuth(login = login, password = password)
			getPage(targetSegment)
		}
		
	}
	
	/**
	 * Get day schedule page
	 *
	 * @param localDate
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws IOException if couldn't parse document
	 * @return parsed document of page
	 */
	private suspend fun getDayPage(localDate: LocalDate): Document {
		val dateFormat = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
		return getPage("/user/diary/day?for=$dateFormat")
	}
	
	/**
	 * Get term page
	 *
	 * @param term to get, could be "year" also
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws IOException if couldn't parse document
	 * @return parsed document of page
	 */
	private suspend fun getTermPage(term: EduPerformancePeriod): Document {
		return if (term != EduPerformancePeriod.YEAR) {
			getPage("/user/diary/term?term=${term.value}")
		} else {
			getPage("/user/diary/year?term=${term.value}")
		}
	}
	
	companion object {
		const val AUTH_COOKIE = "DNSID"
		const val BASE_URL = "https://edu.tatar.ru"
		const val UPDATE_CHANGELOG_URL =
			"https://raw.githubusercontent.com/kosavsech/SchoolDiary/master/app/update-changelog.json"
	}
}