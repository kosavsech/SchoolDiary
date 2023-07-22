package com.kxsv.schooldiary.data.local.features.time_pattern

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.TIME_PATTERN_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface TimePatternDao {
	
	@Query("SELECT * FROM $TIME_PATTERN_TABLE_NAME")
	fun observeAll(): Flow<List<TimePatternEntity>>
	
	@Transaction
	@Query("SELECT * FROM $TIME_PATTERN_TABLE_NAME")
	fun observeAllWithStrokes(): Flow<List<TimePatternWithStrokes>>
	
	@Query("SELECT * FROM $TIME_PATTERN_TABLE_NAME WHERE patternId = :patternId")
	fun observeById(patternId: Long): Flow<TimePatternEntity>
	
	@Transaction
	@Query("SELECT * FROM $TIME_PATTERN_TABLE_NAME WHERE patternId = :patternId")
	fun observeByIdWithStrokes(patternId: Long): Flow<TimePatternWithStrokes>
	
	@Query("SELECT * FROM $TIME_PATTERN_TABLE_NAME")
	suspend fun getAll(): List<TimePatternEntity>
	
	@Transaction
	@Query("SELECT * FROM $TIME_PATTERN_TABLE_NAME WHERE patternId = :id")
	suspend fun getByIdWithStrokes(id: Long): TimePatternWithStrokes?
	
	@Query("SELECT * FROM $TIME_PATTERN_TABLE_NAME WHERE patternId = :patternId")
	suspend fun getById(patternId: Long): TimePatternEntity?
	
	@Upsert
	suspend fun upsertAll(patterns: List<TimePatternEntity>)
	
	@Upsert
	suspend fun upsert(pattern: TimePatternEntity): Long
	
	@Query("DELETE FROM $TIME_PATTERN_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $TIME_PATTERN_TABLE_NAME WHERE patternId = :patternId")
	suspend fun deleteById(patternId: Long): Int
	
	@Transaction
	@Query("DELETE FROM $TIME_PATTERN_TABLE_NAME WHERE patternId = :patternId")
	suspend fun deleteByIdWithStrokes(patternId: Long): Int
}