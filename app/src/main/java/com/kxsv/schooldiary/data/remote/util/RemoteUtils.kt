package com.kxsv.schooldiary.data.remote.util

object RemoteUtils {
	fun handleErrorResponse(result: String) {
		when {
			result.contains(
				"Неверный логин или пароль",
				true
			) -> throw NetworkException.IncorrectAuthDataException
			
			result.contains(
				"Пользователь не найден",
				true
			) -> throw NetworkException.NotLoggedInException
			
			result.contains(
				"Доступ в систему временно заблокирован",
				true
			) -> throw NetworkException.AccessTemporarilyBlockedException
			
			result.contains(
				"Введите логин и пароль",
				true
			) -> throw NetworkException.BlankInputException
		}
	}
}