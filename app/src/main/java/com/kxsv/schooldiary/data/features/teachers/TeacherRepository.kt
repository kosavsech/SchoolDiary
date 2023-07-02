package com.kxsv.schooldiary.data.features.teachers

import kotlinx.coroutines.flow.Flow

interface TeacherRepository {

    fun getTeachersStream(): Flow<List<Teacher>>

    fun getTeacherStream(teacherId: Int): Flow<Teacher>

    suspend fun getTeachers(): List<Teacher>

    suspend fun getTeacher(teacherId: Int): Teacher?

    suspend fun getTeacherWithSubjects(teacherId: Int): TeacherWithSubjects?

    suspend fun createTeacher(teacher: Teacher)

    suspend fun updateTeacher(teacher: Teacher)

    suspend fun deleteTeachers()

    suspend fun deleteTeacher(teacherId: Int)
}