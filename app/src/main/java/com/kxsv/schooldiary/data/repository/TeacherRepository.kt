package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherWithSubjects
import kotlinx.coroutines.flow.Flow

interface TeacherRepository {
	
	fun observeTeachers(): Flow<List<TeacherEntity>>
	
	fun getTeacherStream(teacherId: Int): Flow<TeacherEntity>
	
	suspend fun getTeachers(): List<TeacherEntity>
	
	suspend fun getTeacher(teacherId: Int): TeacherEntity?

    suspend fun getTeacherWithSubjects(teacherId: Int): TeacherWithSubjects?
	
	suspend fun createTeacher(teacher: TeacherEntity)
	
	suspend fun updateTeacher(teacher: TeacherEntity)

    suspend fun deleteTeachers()

    suspend fun deleteTeacher(teacherId: Int)
}