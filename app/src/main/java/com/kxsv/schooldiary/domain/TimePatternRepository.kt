package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.local.features.time_pattern.PatternWithStrokes
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePattern
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStroke
import kotlinx.coroutines.flow.Flow

interface TimePatternRepository {
	
	fun getPatternsStream(): Flow<List<TimePattern>>
	
	fun getPatternsWithStrokesStream(): Flow<List<PatternWithStrokes>>
	
	fun getPatternWithStrokesStream(patternId: Long): Flow<PatternWithStrokes?>
	
	suspend fun getPatterns(): List<TimePattern>
	
	suspend fun getPatternWithStrokes(patternId: Long): PatternWithStrokes?
	
	suspend fun getPattern(patternId: Long): TimePattern?
	
	suspend fun createPattern(pattern: TimePattern): Long
	
	suspend fun createPatternWithStrokes(name: String, strokes: List<PatternStroke>): Long
	
	suspend fun deleteAllPatterns()
	
	suspend fun updatePatternWithStrokes(pattern: TimePattern, strokes: List<PatternStroke>)
	
	suspend fun deletePattern(patternId: Long)
}