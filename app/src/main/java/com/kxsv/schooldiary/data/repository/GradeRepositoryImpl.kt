package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.grade.DayGradeDto
import com.kxsv.schooldiary.data.remote.grade.GradeParser
import com.kxsv.schooldiary.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepositoryImpl @Inject constructor(
	private val gradeDataSource: GradeDao,
	private val webService: WebService,
	@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : GradeRepository {
	
	override fun getGradesStream(): Flow<List<GradeEntity>> {
		return gradeDataSource.observeAll()
	}
	
	override fun getGradesBySubjectIdStream(subjectId: Long): Flow<List<GradeEntity>> {
		return gradeDataSource.observeAllBySubjectId(subjectId)
	}
	
	override fun getGradeStream(gradeId: String): Flow<GradeEntity> {
		return gradeDataSource.observeById(gradeId)
	}
	
	override suspend fun getGrades(): List<GradeEntity> {
		return gradeDataSource.getAll()
	}
	
	override suspend fun fetchGradeByDate(localDate: LocalDate): List<DayGradeDto> {
		val classes = webService.getScheduleForDate(localDate)
		return GradeParser().parse(classes, localDate)
	}
	
	override suspend fun getGradesWithSubjects(): List<GradeWithSubject> {
		return gradeDataSource.getAllWithSubjects()
	}
	
	override suspend fun getGrade(gradeId: String): GradeEntity? {
		return gradeDataSource.getById(gradeId)
	}
	
	override suspend fun getGradeWithSubject(gradeId: String): GradeWithSubject? {
		return gradeDataSource.getByIdWithSubject(gradeId)
	}
	
	override suspend fun upsert(grade: GradeEntity): String {
		val dateStamp =
			grade.date.atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond().toString()
		val gradeIndex = grade.index.toString()
		val lessonIndex = grade.lessonIndex.toString()
		val gradeId = (dateStamp + "_" + gradeIndex + "_" + lessonIndex)
		gradeDataSource.upsert(grade.copy(gradeId = gradeId))
		return gradeId
	}
	
	override suspend fun upsertAll(grades: List<GradeEntity>) {
		gradeDataSource.upsertAll(grades)
	}
	
	override suspend fun deleteAllGrades() {
		gradeDataSource.deleteAll()
	}
	
	override suspend fun deleteGrade(gradeId: String) {
		gradeDataSource.deleteById(gradeId)
	}
	
}