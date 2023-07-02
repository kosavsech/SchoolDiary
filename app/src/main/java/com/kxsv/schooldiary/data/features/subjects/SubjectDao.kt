package com.kxsv.schooldiary.data.features.subjects

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {

    @Query("SELECT * FROM Subject")
    fun observeAll(): Flow<List<Subject>>

    @Query("SELECT * FROM Subject WHERE subjectId = :subjectId")
    fun observeById(subjectId: Long): Flow<Subject>

    @Query("SELECT * FROM Subject")
    suspend fun getAll(): List<Subject>

    @Query("SELECT * FROM Subject WHERE subjectId = :subjectId")
    suspend fun getById(subjectId: Long): Subject?

    @Transaction
    @Query("SELECT * FROM Subject WHERE subjectId = :subjectId")
    suspend fun getByIdWithTeachers(subjectId: Long): SubjectWithTeachers?

    @Upsert
    suspend fun upsertAll(subjects: List<Subject>)

    @Upsert
    suspend fun upsert(subject: Subject): Long

    @Query("DELETE FROM Subject")
    suspend fun deleteAll()

    @Query("DELETE FROM Subject WHERE subjectId = :subjectId")
    suspend fun deleteById(subjectId: Long): Int
}