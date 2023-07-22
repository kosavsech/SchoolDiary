package com.kxsv.schooldiary.data.remote.lesson

import com.kxsv.schooldiary.util.remote.NetLessonColumn
import org.jsoup.select.Elements
import java.time.LocalDate

class ScheduleParser {
	fun parse(lessons: Elements, localDate: LocalDate): List<LessonDto> {
		val schedule = mutableListOf<LessonDto>()
		lessons.forEachIndexed { index, lesson ->
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