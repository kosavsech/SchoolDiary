package com.kxsv.schooldiary.data.features.teachers

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    @Query("SELECT * FROM Teacher ORDER BY patronymic ASC")
    fun observeAll(): Flow<List<Teacher>>

    @Query("SELECT * FROM Teacher WHERE teacherId = :teacherId")
    fun observeById(teacherId: Int): Flow<Teacher>

    @Query("SELECT * FROM Teacher ORDER BY patronymic ASC")
    suspend fun getAll(): List<Teacher>

    @Query("SELECT * FROM Teacher WHERE teacherId = :teacherId")
    suspend fun getById(teacherId: Int): Teacher?

    @Transaction
    @Query("SELECT * FROM Teacher WHERE teacherId = :teacherId")
    suspend fun getByIdWithSubjects(teacherId: Int): TeacherWithSubjects?

    @Upsert
    suspend fun upsertAll(teachers: List<Teacher>)

    @Upsert
    suspend fun upsert(teacher: Teacher)

    @Query("DELETE FROM TEACHER")
    suspend fun deleteAll()

    @Query("DELETE FROM TEACHER WHERE teacherId = :teacherId")
    suspend fun deleteById(teacherId: Int)
}