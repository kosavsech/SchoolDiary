package com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternStrokeDao {

    @Query("SELECT * FROM PatternStroke")
    fun observeAll(): Flow<List<PatternStroke>>

    @Query("SELECT * FROM PatternStroke WHERE patternMasterId = :patternId")
    fun observeAllByPatternId(patternId: Long): Flow<List<PatternStroke>>

    @Query("SELECT * FROM PatternStroke WHERE strokeId = :id")
    fun observeById(id: Int): Flow<PatternStroke>

    @Query("SELECT * FROM PatternStroke")
    suspend fun getAll(): List<PatternStroke>

    @Query("SELECT * FROM PatternStroke WHERE patternMasterId = :patternId")
    suspend fun getAllByPatternId(patternId: Long): List<PatternStroke>

    @Query("SELECT * FROM PatternStroke WHERE strokeId = :id")
    suspend fun getById(id: Int): PatternStroke?

    @Upsert
    suspend fun upsertAll(strokes: List<PatternStroke>)

    @Upsert
    suspend fun upsert(stroke: PatternStroke)

    @Query("DELETE FROM PatternStroke")
    suspend fun deleteAll()

    @Query("DELETE FROM PatternStroke WHERE patternMasterId = :patternId")
    suspend fun deleteAllByPatternId(patternId: Long)

    @Query("DELETE FROM PatternStroke WHERE strokeId = :id")
    suspend fun deleteById(id: Int)
}