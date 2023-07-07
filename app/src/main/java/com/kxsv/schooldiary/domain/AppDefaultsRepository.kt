package com.kxsv.schooldiary.domain

import kotlinx.coroutines.flow.Flow

interface AppDefaultsRepository {
	
	fun getPatternIdStream() : Flow<Long>
	
	suspend fun getPatternId() : Long
	
	suspend fun setPatternId(id: Long)
}