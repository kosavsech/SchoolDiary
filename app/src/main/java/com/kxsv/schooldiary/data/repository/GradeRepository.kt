package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.remote.grade.DayGradeDto
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GradeRepository {
	
	fun getGradesStream(): Flow<List<GradeEntity>>
	
	fun getGradesBySubjectIdStream(subjectId: Long): Flow<List<GradeEntity>>
	
	fun getGradeStream(gradeId: String): Flow<GradeEntity>
	
	suspend fun getGrades(): List<GradeEntity>
	
	suspend fun fetchGradeByDate(localDate: LocalDate): List<DayGradeDto>
	
	suspend fun getGradesWithSubjects(): List<GradeWithSubject>
	
	suspend fun getGrade(gradeId: String): GradeEntity?
	
	suspend fun getGradeWithSubject(gradeId: String): GradeWithSubject?
	
	suspend fun upsert(grade: GradeEntity): String
	
	suspend fun upsertAll(grades: List<GradeEntity>)
	
	suspend fun deleteAllGrades()
	
	suspend fun deleteGrade(gradeId: String)
}