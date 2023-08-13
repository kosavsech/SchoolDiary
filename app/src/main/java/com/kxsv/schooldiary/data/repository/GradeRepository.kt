package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.remote.dtos.DayGradeDto
import com.kxsv.schooldiary.data.remote.dtos.TeacherDto
import com.kxsv.schooldiary.data.util.remote.NetworkException
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GradeRepository {
	
	fun observeAllOrderedByMarkDate(): Flow<List<GradeEntity>>
	
	fun observeAllWithSubjectOrderedByMarkDate(): Flow<List<GradeWithSubject>>
	
	fun observeAllOrderedByFetchDate(): Flow<List<GradeEntity>>
	
	fun observeAllWithSubjectOrderedByFetchDate(): Flow<List<GradeWithSubject>>
	
	fun getGradesBySubjectIdStream(subjectId: String): Flow<List<GradeEntity>>
	
	fun getGradeStream(gradeId: String): Flow<GradeEntity>
	
	suspend fun getGrades(): List<GradeEntity>
	
	suspend fun fetchGradesByDate(localDate: LocalDate): Pair<Map<TeacherDto, Set<String>>, List<DayGradeDto>>
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	suspend fun fetchRecentGradesWithTeachers(): Pair<MutableList<DayGradeDto>, MutableMap<TeacherDto, MutableSet<String>>>
	
	suspend fun getGradesWithSubjects(): List<GradeWithSubject>
	
	suspend fun getGrade(gradeId: String): GradeEntity?
	
	suspend fun getGradeWithSubject(gradeId: String): GradeWithSubject?
	
	suspend fun create(grade: GradeEntity): String
	
	suspend fun update(grade: GradeEntity)
	
	suspend fun upsertAll(grades: List<GradeEntity>)
	
	suspend fun deleteAllGrades()
	
	suspend fun deleteGrade(gradeId: String)
}