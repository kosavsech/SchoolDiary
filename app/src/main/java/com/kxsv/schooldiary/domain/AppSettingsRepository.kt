package com.kxsv.schooldiary.domain

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
	
	fun observePatternId(): Flow<Long>
	
	fun observeScheduleRefRangeStartId(): Flow<Long>
	
	fun observeScheduleRefRangeEndId(): Flow<Long>
	
	fun observeEduLogin(): Flow<String?>
	
	fun observeEduPassword(): Flow<String?>
	
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
	
}