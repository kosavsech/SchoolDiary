package com.kxsv.schooldiary.data.features.time_pattern

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimePatternDao {

    @Query("SELECT * FROM TimePattern")
    fun observeAll(): Flow<List<TimePattern>>

    @Transaction
    @Query("SELECT * FROM TimePattern")
    fun observeAllWithStrokes(): Flow<List<PatternWithStrokes>>

    @Query("SELECT * FROM TimePattern WHERE patternId = :patternId")
    fun observeById(patternId: Long): Flow<TimePattern>

    @Transaction
    @Query("SELECT * FROM TimePattern WHERE patternId = :patternId")
    fun observeByIdWithStrokes(patternId: Long): Flow<PatternWithStrokes>

    @Query("SELECT * FROM TimePattern")
    suspend fun getAll(): List<TimePattern>

    @Transaction
    @Query("SELECT * FROM TimePattern WHERE patternId = :id")
    suspend fun getByIdWithStrokes(id: Long): PatternWithStrokes?

    @Query("SELECT * FROM TimePattern WHERE patternId = :patternId")
    suspend fun getById(patternId: Long): TimePattern?

    @Upsert
    suspend fun upsertAll(patterns: List<TimePattern>)

    @Upsert
    suspend fun upsert(pattern: TimePattern): Long

    @Query("DELETE FROM TimePattern")
    suspend fun deleteAll()

    @Query("DELETE FROM TimePattern WHERE patternId = :patternId")
    suspend fun deleteById(patternId: Long): Int

    @Transaction
    @Query("DELETE FROM TimePattern WHERE patternId = :patternId")
    suspend fun deleteByIdWithStrokes(patternId: Long): Int
}