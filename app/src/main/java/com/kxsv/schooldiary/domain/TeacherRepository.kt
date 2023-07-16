package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.local.features.teacher.Teacher
import com.kxsv.schooldiary.data.local.features.teacher.TeacherWithSubjects
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