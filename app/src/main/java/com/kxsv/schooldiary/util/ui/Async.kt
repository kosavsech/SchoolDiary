package com.kxsv.schooldiary.util.ui

sealed class Async<out T> {
	object Loading : Async<Nothing>()
	
	data class Error(val errorMessage: Int, val formatArg: String? = null) : Async<Nothing>()
	
	data class Success<out T>(val data: T) : Async<T>()
}
