package com.kxsv.schooldiary.util.remote


sealed class NetworkException(message: String? = null) : Exception(message) {
	object AccessTemporarilyBlockedException : NetworkException()
	object BlankInputException : NetworkException()
	object IncorrectAuthDataException : NetworkException()
	object NotLoggedInException : NetworkException()
	
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
			
			else -> NetworkError.GeneralError(this.message)
		}
	}
}