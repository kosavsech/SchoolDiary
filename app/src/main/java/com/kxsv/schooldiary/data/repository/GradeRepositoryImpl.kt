package com.kxsv.schooldiary.data.repository

import android.util.Log
import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.grade.DayGradeDto
import com.kxsv.schooldiary.data.remote.grade.GradeParser
import com.kxsv.schooldiary.di.ApplicationScope
import com.kxsv.schooldiary.di.DefaultDispatcher
import com.kxsv.schooldiary.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GradeRepositoryImpl"

@Singleton
class GradeRepositoryImpl @Inject constructor(
	private val gradeDataSource: GradeDao,
	private val webService: WebService,
	private val subjectDataSource: SubjectDao,
	@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
	@ApplicationScope private val scope: CoroutineScope,
) : GradeRepository {
	private var newGradesFound: Boolean = false
	
	override fun observeAllOrderedByMarkDate(): Flow<List<GradeEntity>> {
		return gradeDataSource.observeAllOrderedByMarkDate()
	}
	
	override fun observeAllWithSubjectOrderedByMarkDate(): Flow<List<GradeWithSubject>> {
		return gradeDataSource.observeAlleWithSubjectOrderedByMarkDate()
	}
	
	override fun observeAllOrderedByFetchDate(): Flow<List<GradeEntity>> {
		return gradeDataSource.observeAllOrderedByFetchDate()
	}
	
	override fun observeAllWithSubjectOrderedByFetchDate(): Flow<List<GradeWithSubject>> {
		return gradeDataSource.observeAlleWithSubjectOrderedByFetchDate()
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
		val dayInfo = webService.getDayInfo(localDate)
		return GradeParser().parse(dayInfo, localDate)
	}
	
	override suspend fun fetchRecentGrades(): Unit = withContext(ioDispatcher) {
		var daysChecked = 0
		var counter = 0
		while (daysChecked != 14) {
			// todo change to NOW
			val date = LocalDate.of(2023, 2, 19).minusDays(counter.toLong())
			if (date.dayOfWeek != DayOfWeek.SUNDAY) {
				async {
					val gradesLocalised = fetchGradeByDate(date).toGradeEntities()
					updateDatabase(gradesLocalised)
				}
				daysChecked++
			}
			counter++
		}
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
	
	override suspend fun create(grade: GradeEntity): String {
		val gradeId =
			generateGradeId(date = grade.date, index = grade.index, lessonIndex = grade.lessonIndex)
		gradeDataSource.upsert(grade.copy(gradeId = gradeId))
		return gradeId
	}
	
	override suspend fun update(grade: GradeEntity) {
		gradeDataSource.upsert(grade)
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
	
	private suspend fun updateDatabase(gradeEntities: List<GradeEntity>) = scope.launch {
		for (gradeEntity in gradeEntities) {
			val gradeFound = gradeDataSource.getById(gradeEntity.gradeId) != null
			if (gradeFound) {
				update(gradeEntity)
			} else {
				create(gradeEntity)
				newGradesFound = true
				Log.i(TAG, "updateDatabase: FOUND NEW GRADE $gradeEntity")
			}
		}
	}
	
	private suspend fun DayGradeDto.toGradeEntity(): GradeEntity {
		try {
			val subjectMasterId =
				subjectDataSource.getByName(subjectAncestorName)?.subjectId
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			// TODO: add prompt to create subject with such name, or create it forcibly
			return GradeEntity(
				mark = mark,
				date = date,
				fetchDateTime = LocalDateTime.now(),
				subjectMasterId = subjectMasterId,
				typeOfWork = typeOfWork,
				index = index,
				lessonIndex = lessonIndex,
				gradeId = generateGradeId(date = date, index = index, lessonIndex = lessonIndex)
			)
			
		} catch (e: NoSuchElementException) {
			throw RuntimeException("Failed to convert network to local", e)
		}
	}
	
	private suspend fun List<DayGradeDto>.toGradeEntities() = map { it.toGradeEntity() }
	
	private fun generateGradeId(date: LocalDate, index: Int, lessonIndex: Int): String {
		val dateStamp = date.atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond().toString()
		val gradeIndex = index.toString()
		val lessonIndexString = lessonIndex.toString()
		return (dateStamp + "_" + gradeIndex + "_" + lessonIndexString)
	}
}