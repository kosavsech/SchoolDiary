package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.local.features.grade.Grade
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import kotlinx.coroutines.flow.Flow

interface GradeRepository {
	
	fun getGradesStream(): Flow<List<Grade>>
	
	fun getGradesBySubjectIdStream(subjectId: Long): Flow<List<Grade>>
	
	fun getGradeStream(gradeId: Long): Flow<Grade>
	
	suspend fun getGrades(): List<Grade>
	
	suspend fun getGradesWithSubjects(): List<GradeWithSubject>
	
	suspend fun getGrade(gradeId: Long): Grade?
	
	suspend fun getGradeWithSubject(gradeId: Long): GradeWithSubject?
	
	suspend fun createGrade(grade: Grade): Long
	
	suspend fun updateGrade(grade: Grade)
	
	suspend fun upsertAll(grades: List<Grade>)
	
	suspend fun deleteAllGrades()
	
	suspend fun deleteGrade(gradeId: Long)
}