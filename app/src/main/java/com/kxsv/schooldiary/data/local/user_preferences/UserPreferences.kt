package com.kxsv.schooldiary.data.local.user_preferences

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
	val defaultTargetMark: Double = 4.6,
	val defaultPatternId: Long = 0L,
	val scheduleRefRangeStartId: Long = 0L,
	val scheduleRefRangeEndId: Long = 0L,
	val suppressInitLogin: Boolean = false,
	val eduLogin: String? = null,
	val eduPassword: String? = null,
	val authCookie: String? = null,
)