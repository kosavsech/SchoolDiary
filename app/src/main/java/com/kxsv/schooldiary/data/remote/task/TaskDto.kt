package com.kxsv.schooldiary.data.remote.task

import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import java.time.LocalDate

data class TaskDto(
	val title: String,
	val subject: SubjectEntity,
	val dueDate: LocalDate,
	val lessonIndex: Int,
)
