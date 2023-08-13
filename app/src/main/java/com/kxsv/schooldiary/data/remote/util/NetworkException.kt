package com.kxsv.schooldiary.data.remote.util


sealed class NetworkException(message: String? = null) : Exception(message) {
	object AccessTemporarilyBlockedException : NetworkException()
	object BlankInputException : NetworkException()
	object IncorrectAuthDataException : NetworkException()
	object NotLoggedInException : NetworkException()
	object NotActualAuthSessionException : NetworkException()
	
	fun mapToNetworkError(): NetworkError {
		return when (this) {
			AccessTemporarilyBlockedException -> {
				NetworkError.AccessTemporarilyBlocked
			}
			
			BlankInputException -> {
				NetworkError.BlankInput
			}
			
			IncorrectAuthDataException -> {
				NetworkError.IncorrectAuthData
			}
			
			NotLoggedInException -> {
				NetworkError.NotLoggedIn
			}
			
			NotActualAuthSessionException -> {
				NetworkError.NotActualAuthSession
			}
			
			else -> NetworkError.GeneralError(this.message)
		}
	}
}