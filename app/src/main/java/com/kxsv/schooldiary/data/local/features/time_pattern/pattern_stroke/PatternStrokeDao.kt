package com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.PATTERN_STROKE_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternStrokeDao {
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME ORDER BY startTime ASC")
	fun observeAll(): Flow<List<PatternStrokeEntity>>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME WHERE patternMasterId = :patternId ORDER BY startTime ASC")
	fun observeAllByPatternId(patternId: Long): Flow<List<PatternStrokeEntity>>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME WHERE strokeId = :id ORDER BY startTime ASC")
	fun observeById(id: Int): Flow<PatternStrokeEntity>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME ORDER BY startTime ASC")
	suspend fun getAll(): List<PatternStrokeEntity>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME WHERE patternMasterId = :patternId ORDER BY startTime ASC")
	suspend fun getAllByPatternId(patternId: Long): List<PatternStrokeEntity>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME WHERE strokeId = :id")
	suspend fun getById(id: Int): PatternStrokeEntity?
	
	@Upsert
	suspend fun upsertAll(strokes: List<PatternStrokeEntity>)
	
	@Upsert
	suspend fun upsert(stroke: PatternStrokeEntity)
	
	@Query("DELETE FROM $PATTERN_STROKE_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $PATTERN_STROKE_TABLE_NAME WHERE patternMasterId = :patternId")
	suspend fun deleteAllByPatternId(patternId: Long)
	
	@Query("DELETE FROM $PATTERN_STROKE_TABLE_NAME WHERE strokeId = :id")
	suspend fun deleteById(id: Int)
}