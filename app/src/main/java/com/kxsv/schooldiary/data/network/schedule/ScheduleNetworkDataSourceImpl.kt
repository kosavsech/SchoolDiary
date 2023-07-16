package com.kxsv.schooldiary.data.network.schedule

import com.kxsv.schooldiary.data.network.ScheduleNetworkDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ScheduleNetworkDataSourceImpl @Inject constructor() : ScheduleNetworkDataSource {
	private var mapCookies: Map<String, String> = mapOf()
	
	// A mutex is used to ensure that reads and writes are thread-safe.
	private val accessMutex = Mutex()
	override suspend fun loadScheduleForDate(localDate: LocalDate): List<NetworkSchedule> =
		accessMutex.withLock {
			val schedule = mutableListOf<NetworkSchedule>()
			try {
				val response = withContext(Dispatchers.IO) {
					Jsoup.connect("https://edu.tatar.ru/logon")
						.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63")
						.method(Connection.Method.POST)
						.data("main_login2", "5199011556")
						.data("main_password2", "T25Z")
						.referrer("https://edu.tatar.ru/login/")
						.followRedirects(true)
						.execute()
				}
				mapCookies = response.cookies()
				val dayPage = getDayPage(localDate)
				val lessons = dayPage.select("div.d-table > table > tbody > tr")
				val lessonsAmount = lessons.size
				for (i in 0 until lessonsAmount) {
					val subjectAncestorName =
						lessons.select("tr:nth-child(${i + 1}) > td:nth-child(2)").text()
					schedule.add(
						NetworkSchedule(
							index = i,
							date = localDate,
							subjectAncestorName = subjectAncestorName
						)
					)
				}
			} catch (e: IOException) {
				e.printStackTrace()
			}
			return schedule
		}
	
	private suspend fun getPage(targetSegment: String): Document {
		val doc = withContext(Dispatchers.IO) {
			Jsoup.connect("https://edu.tatar.ru/$targetSegment")
				.cookies(mapCookies)
				.get()
		}
		return doc
	}
	
	private suspend fun getDayPage(localDate: LocalDate): Document {
		val dateFormat = localDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
		return getPage("user/diary/day?for=$dateFormat")
	}
}