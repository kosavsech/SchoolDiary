package com.kxsv.schooldiary.data.remote.util

sealed class NetworkError {
	object AccessTemporarilyBlocked : NetworkError()
	object BlankInput : NetworkError()
	object IncorrectAuthData : NetworkError()
	object NotLoggedIn : NetworkError()
	object NotActualAuthSession : NetworkError()
	object PageNotFound : NetworkError()
	data class GeneralError(
		val message: String? = null,
		val cause: Throwable? = null,
	) : NetworkError()
}
