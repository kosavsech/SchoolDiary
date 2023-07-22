package com.kxsv.schooldiary.data.remote.grade

import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.remote.NetLessonColumn
import org.jsoup.select.Elements
import java.time.LocalDate

class GradeParser {
	private data class GradeItem(
		val value: String,
		val typeOfWork: String = "",
		val index: Int = 0,
	)
	
	fun parse(classes: Elements, localDate: LocalDate): List<DayGradeDto> {
		val grades = mutableListOf<DayGradeDto>()
		classes.forEachIndexed { lessonIndex, lesson ->
			val lessonsGradeItems = mutableListOf<GradeItem>()
			val marks =
				lesson.child(NetLessonColumn.GRADE.ordinal).children().select("tbody > tr > td")
			if (marks.size != 0) {
				marks.forEachIndexed { index, mark ->
					val value = mark.select("td").text()
					val typeOfWork = mark.select("td").attr("title").split(" - ")[1]
					lessonsGradeItems.add(
						GradeItem(
							value = value,
							typeOfWork = typeOfWork,
							index = index
						)
					)
				}
			} else {
				lesson.child(NetLessonColumn.COMMENT.ordinal).text().let { comment ->
					if (comment.isNotBlank()) lessonsGradeItems.add(GradeItem(value = comment))
				}
			}
			val subjectAncestorName = lesson.child(NetLessonColumn.SUBJECT.ordinal).text()
			lessonsGradeItems.forEach { gradeItem ->
				grades.add(
					DayGradeDto(
						mark = Mark.fromInput(gradeItem.value),
						typeOfWork = gradeItem.typeOfWork,
						date = localDate,
						subjectAncestorName = subjectAncestorName,
						index = gradeItem.index,
						lessonIndex = lessonIndex
					)
				)
			}
		}
		return grades
	}
}