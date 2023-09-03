package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternWithStrokes
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import kotlinx.coroutines.flow.Flow

interface TimePatternRepository {
	
	fun getPatternsStream(): Flow<List<TimePatternEntity>>
	
	fun observePatternsWithStrokes(): Flow<List<TimePatternWithStrokes>>
	
	fun getPatternWithStrokesStream(patternId: Long): Flow<TimePatternWithStrokes?>
	
	suspend fun getPatterns(): List<TimePatternEntity>
	
	suspend fun getPatternWithStrokes(patternId: Long): TimePatternWithStrokes?
	
	suspend fun getPattern(patternId: Long): TimePatternEntity?
	
	suspend fun createPattern(pattern: TimePatternEntity): Long
	
	suspend fun createPatternWithStrokes(name: String, strokes: List<PatternStrokeEntity>): Long
	
	suspend fun deleteAllPatterns()
	
	suspend fun updatePatternWithStrokes(
		pattern: TimePatternEntity,
		strokes: List<PatternStrokeEntity>,
	)
	
	suspend fun deletePattern(patternId: Long)
}