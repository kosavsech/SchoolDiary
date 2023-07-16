package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStroke
import kotlinx.coroutines.flow.Flow

interface PatternStrokeRepository {

    fun getStrokesStream(): Flow<List<PatternStroke>>

    fun getStrokesStreamByPatternId(patternId: Long): Flow<List<PatternStroke>>

    fun getStrokeStream(id: Int): Flow<PatternStroke>

    suspend fun getStrokes(): List<PatternStroke>

    suspend fun getStrokesByPatternId(patternId: Long): List<PatternStroke>

    suspend fun getStroke(id: Int): PatternStroke?

    suspend fun createStrokes(patternStrokes: List<PatternStroke>)

    suspend fun createStroke(patternStroke: PatternStroke)

    suspend fun updateStrokes(patternStrokes: List<PatternStroke>)

    suspend fun updateStroke(patternStroke: PatternStroke)

    suspend fun deleteAll()
    
    suspend fun deleteStrokeById(id: Int)
}