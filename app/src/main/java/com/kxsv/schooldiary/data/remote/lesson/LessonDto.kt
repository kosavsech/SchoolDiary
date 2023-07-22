package com.kxsv.schooldiary.data.remote.lesson

import java.time.LocalDate

data class LessonDto(
	val index: Int,
	val date: LocalDate,
	val subjectAncestorName: String,
)