package com.kxsv.schooldiary.data.remote.parsers

import android.util.Log
import com.kxsv.schooldiary.data.remote.dtos.DayGradeDto
import com.kxsv.schooldiary.data.remote.dtos.TeacherDto
import com.kxsv.schooldiary.data.util.remote.NetLessonColumn
import com.kxsv.schooldiary.util.Mark
import org.jsoup.select.Elements
import java.time.LocalDate

private const val TAG = "DayGradeParser"

class DayGradeParser {
	private data class GradeItem(
		val value: String,
		val typeOfWork: String = "",
		val index: Int = 0,
	)
	
	fun parse(
		classes: Elements,
		localDate: LocalDate,
	): Pair<Map<TeacherDto, Set<String>>, List<DayGradeDto>> {
		val teachers = mutableMapOf<TeacherDto, MutableSet<String>>()
		val grades = mutableListOf<DayGradeDto>()
		classes.forEachIndexed { lessonIndex, lesson ->
			val subjectAncestorName = lesson.child(NetLessonColumn.SUBJECT.ordinal).text()
			val lessonsGradeItems = mutableListOf<GradeItem>()
			val marks =
				lesson.child(NetLessonColumn.GRADE.ordinal).children().select("tbody > tr > td")
			if (marks.size != 0) {
				marks.forEachIndexed { index, mark ->
					val value = mark.select("td").text().trim()
					val markDescription = mark.select("td").attr("title")
					val typeOfWork = markDescription.split(" - ")[1]
					lessonsGradeItems.add(
						GradeItem(
							value = value,
							typeOfWork = typeOfWork,
							index = index
						)
					)
					val teacherFullName = markDescription.split(" - ")[0]
					val teacherDto = TeacherDto(
						lastName = teacherFullName.split(" ")[0].trim(),
						firstName = teacherFullName.split(" ")[1].trim(),
						patronymic = teacherFullName.split(" ")[2].trim(),
					)
					val subjectNames = teachers.getOrDefault(teacherDto, mutableSetOf())
					subjectNames.add(subjectAncestorName)
					teachers[teacherDto] = subjectNames
				}
			} else {
				lesson.child(NetLessonColumn.COMMENT.ordinal).text().let { comment ->
					if (comment.isNotBlank()) lessonsGradeItems.add(GradeItem(value = comment))
				}
			}
			lessonsGradeItems.forEach { gradeItem ->
				grades.add(
					DayGradeDto(
						mark = Mark.fromInput(gradeItem.value)!!,
						typeOfWork = gradeItem.typeOfWork,
						date = localDate,
						subjectAncestorName = subjectAncestorName,
						index = gradeItem.index,
						lessonIndex = lessonIndex
					)
				)
			}
		}
		Log.d(TAG, "parse() returned:\n $teachers")
		return Pair(teachers, grades)
	}
}