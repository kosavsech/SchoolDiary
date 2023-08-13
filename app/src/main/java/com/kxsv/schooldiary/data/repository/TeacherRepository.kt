package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherWithSubjects
import kotlinx.coroutines.flow.Flow

interface TeacherRepository {
	
	fun observeTeachers(): Flow<List<TeacherEntity>>
	
	fun getTeacherStream(teacherId: String): Flow<TeacherEntity>
	
	suspend fun getTeachers(): List<TeacherEntity>
	
	suspend fun getById(teacherId: String): TeacherEntity?
	
	suspend fun getByFullName(
		lastName: String,
		firstName: String,
		patronymic: String,
	): TeacherEntity?
	
	suspend fun getTeacherWithSubjects(teacherId: String): TeacherWithSubjects?
	
	suspend fun createTeacher(teacher: TeacherEntity): String
	
	suspend fun upsert(teacher: TeacherEntity)
	
	suspend fun update(teacher: TeacherEntity)
	
	suspend fun deleteTeachers()
	
	suspend fun deleteTeacher(teacherId: String)
}