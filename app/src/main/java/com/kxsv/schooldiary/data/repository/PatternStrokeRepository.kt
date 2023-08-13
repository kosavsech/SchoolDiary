package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface PatternStrokeRepository {
	
	fun getStrokesStream(): Flow<List<PatternStrokeEntity>>
	
	fun getStrokesStreamByPatternId(patternId: Long): Flow<List<PatternStrokeEntity>>
	
	fun observeAllWithStrokesForDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Flow<Map<LocalDate, List<PatternStrokeEntity>>>
	
	fun getStrokeStream(id: Int): Flow<PatternStrokeEntity>
	
	suspend fun getStrokes(): List<PatternStrokeEntity>
	
	suspend fun getStrokesByPatternId(patternId: Long): List<PatternStrokeEntity>
	
	suspend fun getStroke(id: Int): PatternStrokeEntity?
	
	suspend fun createStrokes(patternStrokes: List<PatternStrokeEntity>)
	
	suspend fun createStroke(patternStroke: PatternStrokeEntity)
	
	suspend fun updateStrokes(patternStrokes: List<PatternStrokeEntity>)
	
	suspend fun updateStroke(patternStroke: PatternStrokeEntity)
	
	suspend fun deleteAll()
	
	suspend fun deleteStrokeById(id: Int)
}