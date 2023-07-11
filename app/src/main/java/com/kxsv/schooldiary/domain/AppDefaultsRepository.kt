package com.kxsv.schooldiary.domain

import kotlinx.coroutines.flow.Flow

interface AppDefaultsRepository {
	
	fun observePatternId(): Flow<Long>
	
	suspend fun getPatternId(): Long
	
	suspend fun setPatternId(id: Long)
	
	fun observeScheduleRefRangeStartId(): Flow<Long>
	
	fun observeScheduleRefRangeEndId(): Flow<Long>
	
	suspend fun getScheduleRefRangeStartId(): Long
	
	suspend fun setScheduleRefRangeStartId(id: Long)
	
	suspend fun getScheduleRefRangeEndId(): Long
	
	suspend fun setScheduleRefRangeEndId(id: Long)
}