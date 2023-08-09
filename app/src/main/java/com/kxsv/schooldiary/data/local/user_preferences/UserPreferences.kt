package com.kxsv.schooldiary.data.local.user_preferences

import kotlinx.serialization.Serializable

@Serializable
data class UserPreferences(
	val defaultTargetMark: Double = 4.6,
	val suppressInitLogin: Boolean = false,
	val defaultPatternId: Long = 0L,
	val eduLogin: String? = null,
	val eduPassword: String? = null,
	val authCookie: String? = null,
)