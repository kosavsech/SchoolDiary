package com.kxsv.schooldiary.data.network.schedule

import java.time.LocalDate

data class NetworkSchedule(
	val index: Int,
	val date: LocalDate,
	val subjectAncestorName: String,
)