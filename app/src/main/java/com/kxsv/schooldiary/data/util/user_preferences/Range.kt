package com.kxsv.schooldiary.data.util.user_preferences

import kotlinx.serialization.Serializable

@Serializable
data class Range(
	val start: String,
	val end: String,
)