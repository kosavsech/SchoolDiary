package com.kxsv.schooldiary.data.remote.parsers

import com.kxsv.schooldiary.data.remote.dtos.LessonDto
import com.kxsv.schooldiary.data.util.remote.NetLessonColumn
import org.jsoup.select.Elements
import java.time.LocalDate

class LessonParser {
	fun parse(dayInfo: Elements, localDate: LocalDate): List<LessonDto> {
		val schedule = mutableListOf<LessonDto>()
		dayInfo.forEachIndexed { index, lesson ->
			val subjectAncestorName = lesson.child(NetLessonColumn.SUBJECT.ordinal).text()
			if (subjectAncestorName.isNotBlank()) {
				schedule.add(
					LessonDto(
						index = index,
						date = localDate,
						subjectAncestorName = subjectAncestorName
					)
				)
			}
		}
		return schedule
	}
}