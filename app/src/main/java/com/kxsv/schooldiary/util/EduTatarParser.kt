package com.kxsv.schooldiary.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/*val response = withContext(Dispatchers.IO) {
    Jsoup.connect("https://edu.tatar.ru/logon")
        .method(Connection.Method.POST)
        .data("main_login2", login)
        .data("main_password2", password)
        .followRedirects(true)
        .execute()
}*/
class EduTatarParser {
    private var mapCookies: Map<String, String> = mapOf()
    private lateinit var response: Document
    suspend fun Auth(login: String, password: String): Connection.Response {
        val response = withContext(Dispatchers.IO) {
            Jsoup.connect("https://edu.tatar.ru/logon")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.63")
                .method(Connection.Method.POST)
                .data("main_login2", login)
                .data("main_password2", password)
                .referrer("https://edu.tatar.ru/login/")
                .followRedirects(true)
                .execute()
        }
        //mapCookies = response.cookies()
        return response
    }

    private suspend fun GetPage(targetSegment: String): Document {
        val doc = withContext(Dispatchers.IO) {
            Jsoup.connect("https://edu.tatar.ru/$targetSegment")
                .cookies(mapCookies)
                .get()
        }
        return doc
    }

    private fun ConvertDateToEpoch(date: String): Long {
        val todayLocalDate =
            when {
                date.isEmpty() -> LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                else -> LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    .atStartOfDay(ZoneId.systemDefault())
            }
        return todayLocalDate.toEpochSecond()
    }

    private fun ConvertDateToDayOfMonth(date: String): Int {
        val todayLocalDate =
            when {
                date.isEmpty() -> LocalDate.now().atStartOfDay(ZoneId.systemDefault())
                else -> LocalDate.parse(date, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                    .atStartOfDay(ZoneId.systemDefault())
            }
        return todayLocalDate.dayOfMonth
    }

    private suspend fun GetDayPage(date: String = ""): Document {
        val dayStamp = ConvertDateToEpoch(date)
        return GetPage("user/diary/day?for=$dayStamp")
    }

    private suspend fun GetTermPage(termNumber: Int = 1): Document {
        return GetPage("user/diary/term?term=$termNumber")
    }

    private suspend fun GetWeekPage(date: String = ""): Document {
        val dayStamp = ConvertDateToEpoch(date)
        return GetPage("user/diary/week?date=$dayStamp")
    }

    suspend fun GetSubjectsFullName() {
        val names: MutableList<String> = mutableListOf()
        val trLesson = GetTermPage().select("tbody > tr > td:first-child")
        trLesson.forEachIndexed { index, element ->
            if (trLesson.lastIndex == index) return@forEachIndexed
            names.add(element.text())
        }
        names.sort()
    }

    // TODO
    /*suspend fun GetSubjectsShortName() {
        val lessons: MutableSet<String> = mutableSetOf()
        //val daysOnPage = dayPage.select("tbody > tr > td.tt-days > div > span")
        var tries = 0
        val dayStamp = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
        var dayPage: Document = GetPage("user/diary/week?for=$dayStamp")

        while ((lessons.size < DB.subjectFullNames.size) and (tries <= 4)) {
            tries++
            val lessonsName = dayPage.select("tbody > tr > td.tt-subj > div")
            lessonsName.forEach { element ->
                if (element.text().isNotEmpty()) lessons.add(element.text())
            }

            dayPage = withContext(Dispatchers.IO) {
                Jsoup.connect(
                    dayPage.getElementsByClass("week-selector-controls").select("a:first-child")
                        .attr("href")
                )
                    .cookies(mapCookies)
                    .get()
            }
        }
        DB.subjectShortNames.addAll(lessons)
        DB.subjectShortNames.sort()
    }*/

/*    suspend fun GetWeekSubjectsShortName(date: String = ""): MutableList<String> {
        val lessons = mutableListOf<String>()
        var dayPage = GetWeekPage(date)
        val lines = dayPage.select("tbody > tr")
        var foundDay = false
        lines.forEach { line ->
            if (!foundDay) {
                val day = line.select("td.tt-days > div")
                if (day.size > 0 && day.text().isDigitsOnly()) {
                    if (day.text().toString().toInt() == ConvertDateToDayOfMonth(date)) {
                        foundDay = true
                    } else return@forEach
                } else return@forEach
            }

            val subjectName = line.select("td.tt-subj").text().toString()
            if (subjectName.isEmpty()) return lessons
            lessons.add(subjectName)
        }
        return lessons
    }*/

    suspend fun GetColumnFromDay(columnName: String, date: String = ""): MutableList<String> {
        val output = mutableListOf<String>()
        val dayPage = GetDayPage(date)
        val column: Int =
            when (columnName) {
                "time" -> 1
                "subject" -> 2
                "task" -> 3
                "comment" -> 4
                "grade" -> 5
                else -> 2 // TODO throw exception
            }
        val lines = dayPage.select("div.d-table > table > tbody > tr > td:nth-child($column)")
        lines.forEach {
            output.add(it.text())
        }
        return output
    }

    suspend fun GetMarkDescription(date: String = ""): MutableList<String> {
        val output = mutableListOf<String>()
        val dayPage = GetDayPage(date)
        val lines = dayPage.select("div.d-table > table > tbody > tr > td:nth-child(5)")
        lines.forEach {
            if (it.childNodeSize() < 3) {
                output.add("")
                return@forEach
            }
            output.add(it.select("table > tbody > tr > td").attr("title"))
        }
        return output
    }

    fun GetTypeOfWork(text: String = "Фамилия Имя Отчетство - Вид работ"): String {
        return text.split(" - ")[0]
    }

    fun GetTeacher(text: String = "Фамилия Имя Отчетство - Вид работ"): String {
        return text.split(" - ")[1]
    }


    companion object {
        fun getInstance(): EduTatarParser {
            return EduTatarParser()
        }
    }
}
