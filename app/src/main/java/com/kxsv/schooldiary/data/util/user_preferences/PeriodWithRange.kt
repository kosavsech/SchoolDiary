package com.kxsv.schooldiary.data.util.user_preferences

import kotlinx.serialization.Serializable

@Serializable
data class PeriodWithRange(
	val period: Period,
	val range: Range,
)