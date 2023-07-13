package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.features.grade.Grade
import kotlinx.coroutines.flow.Flow

interface GradeRepository {
	
	fun getGradesStream(): Flow<List<Grade>>
	
	fun getGradesBySubjectIdStream(subjectId: Long): Flow<List<Grade>>
	
	fun getGradeStream(gradeId: Long): Flow<Grade>
	
	suspend fun getGrades(): List<Grade>
	
	suspend fun getGrade(gradeId: Long): Grade?
	
	suspend fun createGrade(grade: Grade): Long
	
	suspend fun updateGrade(grade: Grade)
	
	suspend fun deleteAllGrades()
	
	suspend fun deleteGrade(gradeId: Long)
}