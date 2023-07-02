package com.kxsv.schooldiary.data.features.time_pattern

import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import kotlinx.coroutines.flow.Flow

interface TimePatternRepository {

    fun getPatternsStream(): Flow<List<TimePattern>>

    fun getPatternsWithStrokesStream(): Flow<List<PatternWithStrokes>>

    fun getPatternWithStrokesStream(patternId: Long): Flow<PatternWithStrokes?>

    suspend fun getPatterns(): List<TimePattern>

    suspend fun getPatternWithStrokes(patternId: Long): PatternWithStrokes?

    suspend fun createPattern(pattern: TimePattern): Long

    suspend fun createPatternWithStrokes(name: String, strokes: List<PatternStroke>): Long

    suspend fun deleteAllPatterns()

    // TODO: remove fields add entity
    suspend fun updatePatternWithStrokes(patternId: Long, name: String, strokes: List<PatternStroke>)

    suspend fun deletePattern(patternId: Long)
}