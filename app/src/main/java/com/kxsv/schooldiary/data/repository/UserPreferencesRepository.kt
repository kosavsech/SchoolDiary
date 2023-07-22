package com.kxsv.schooldiary.data.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
	
	fun observePatternId(): Flow<Long>
	
	fun observeScheduleRefRangeStartId(): Flow<Long>
	
	fun observeScheduleRefRangeEndId(): Flow<Long>
	
	fun observeEduLogin(): Flow<String?>
	
	fun observeEduPassword(): Flow<String?>
	
	fun observeAuthCookie(): Flow<String?>
	
	fun observeInitLoginSuppression(): Flow<Boolean>
	
	suspend fun getPatternId(): Long
	
	suspend fun setPatternId(id: Long)
	
	suspend fun getScheduleRefRangeStartId(): Long
	
	suspend fun setScheduleRefRangeStartId(id: Long)
	
	suspend fun getScheduleRefRangeEndId(): Long
	
	suspend fun setScheduleRefRangeEndId(id: Long)
	
	suspend fun getEduLogin(): String?
	
	suspend fun setEduLogin(login: String?)
	
	suspend fun getEduPassword(): String?
	
	suspend fun setEduPassword(password: String?)
	
	suspend fun getAuthCookie(): String?
	
	suspend fun setAuthCookie(cookie: String?)
	
	suspend fun getInitLoginSuppression(): Boolean
	
	suspend fun setInitLoginSuppression(value: Boolean)
}