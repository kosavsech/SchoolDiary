package com.kxsv.schooldiary.data.remote.dtos

import com.kxsv.schooldiary.util.Mark
import java.time.LocalDate

data class DayGradeDto(
	val mark: Mark,
	val typeOfWork: String,
	val date: LocalDate,
	val subjectAncestorName: String,
	val index: Int,
	val lessonIndex: Int,
)