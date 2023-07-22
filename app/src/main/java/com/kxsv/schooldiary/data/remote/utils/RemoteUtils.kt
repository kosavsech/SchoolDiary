package com.kxsv.schooldiary.data.remote.utils

import com.kxsv.schooldiary.util.remote.NetworkException

object RemoteUtils {
	fun handleErrorResponse(result: String) {
		if (result.contains(
				"Неверный логин или пароль",
				true
			)
		) throw NetworkException.IncorrectAuthDataException
		else if (result.contains(
				"Пользователь не найден",
				true
			)
		) throw NetworkException.NotLoggedInException
		else if (result.contains(
				"Доступ в систему временно заблокирован",
				true
			)
		) throw NetworkException.AccessTemporarilyBlockedException
		else if (result.contains(
				"Введите логин и пароль",
				true
			)
		) throw NetworkException.BlankInputException
	}
}