package com.kxsv.schooldiary.util

class EduTatarParser {
	/*suspend fun GetSubjectsFullName() {
		val names: MutableList<String> = mutableListOf()
		val trLesson = GetTermPage().select("tbody > tr > td:first-child")
		trLesson.forEachIndexed { index, element ->
			if (trLesson.lastIndex == index) return@forEachIndexed
			names.add(element.text())
		}
		names.sort()
	}

	suspend fun GetSubjectsShortName() {
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
	}

    suspend fun GetWeekSubjectsShortName(date: String = ""): MutableList<String> {
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
    }

	*/
}