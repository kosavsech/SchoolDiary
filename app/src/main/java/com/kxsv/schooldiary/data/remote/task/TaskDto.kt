package com.kxsv.schooldiary.data.remote.task

import java.time.LocalDate

data class TaskDto(
	val title: String,
	val subjectId: Long,
	val dueDate: LocalDate,
	val lessonIndex: Int,
)
