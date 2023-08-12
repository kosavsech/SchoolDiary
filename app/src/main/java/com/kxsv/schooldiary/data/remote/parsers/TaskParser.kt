package com.kxsv.schooldiary.data.remote.parsers

import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.remote.dtos.TaskDto
import com.kxsv.schooldiary.data.util.remote.NetLessonColumn
import org.jsoup.select.Elements
import java.time.LocalDate

class TaskParser {
	fun parse(dayInfo: Elements, date: LocalDate, subject: SubjectEntity): List<TaskDto> {
		val tasks = mutableListOf<TaskDto>()
		dayInfo.forEachIndexed { index, lesson ->
			val subjectAncestorName = lesson.child(NetLessonColumn.SUBJECT.ordinal).text()
			if (subjectAncestorName != subject.fullName) return@forEachIndexed
			if (lesson.child(NetLessonColumn.TASK.ordinal).text().isNotEmpty()) {
				tasks.add(
					TaskDto(
						title = lesson.child(NetLessonColumn.TASK.ordinal).text(),
						subject = subject,
						dueDate = date,
						lessonIndex = index
					)
				)
			}
		}
		return tasks
	}
	
}