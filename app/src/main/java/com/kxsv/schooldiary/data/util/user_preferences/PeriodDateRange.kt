package com.kxsv.schooldiary.data.util.user_preferences

import kotlinx.serialization.Serializable

@Serializable
data class PeriodDateRange(
	val start: String,
	val end: String,
)