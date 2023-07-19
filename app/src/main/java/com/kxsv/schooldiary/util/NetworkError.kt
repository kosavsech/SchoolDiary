package com.kxsv.schooldiary.util

sealed class NetworkError {
	object AccessTemporarilyBlocked : NetworkError()
	object BlankInput : NetworkError()
	object IncorrectAuthData : NetworkError()
	object NotLoggedIn : NetworkError()
	data class GeneralError(
		val message: String? = null,
		val cause: Throwable? = null,
	) : NetworkError()
}
