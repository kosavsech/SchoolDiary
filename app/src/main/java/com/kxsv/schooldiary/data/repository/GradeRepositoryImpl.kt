package com.kxsv.schooldiary.data.repository

import android.util.Log
import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.grade.DayGradeDto
import com.kxsv.schooldiary.data.remote.grade.GradeParser
import com.kxsv.schooldiary.di.util.DefaultDispatcher
import com.kxsv.schooldiary.di.util.IoDispatcher
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.measurePerformanceInMS
import com.kxsv.schooldiary.util.Utils.toList
import com.kxsv.schooldiary.util.remote.NetworkException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
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
) : GradeRepository {
	
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
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	override suspend fun fetchGradeByDate(localDate: LocalDate): List<DayGradeDto> {
		val dayInfo = webService.getDayInfo(localDate)
		return GradeParser().parse(dayInfo, localDate)
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	override suspend fun fetchRecentGrades(): List<GradeWithSubject> {
		return withContext(ioDispatcher) {
			withTimeout(15000L) {
				val newGradesFound: MutableList<GradeWithSubject> = mutableListOf()
				// todo change to NOW
				val startRange = Utils.currentDate
				val period = (startRange.minusDays(14)..startRange).toList()
				period.forEach { date ->
					if (date.dayOfWeek == DayOfWeek.SUNDAY) return@forEach
					async {
						val gradesWithSubjectLocalised =
							fetchGradeByDate(date).toGradesWithSubject()
						newGradesFound.addAll(updateDatabase(gradesWithSubjectLocalised))
					}
				}
				return@withTimeout newGradesFound
			}
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
			generateGradeId(
				date = grade.date,
				index = grade.index,
				lessonIndex = grade.lessonIndex
			)
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
	
	private suspend fun updateDatabase(gradeEntities: List<GradeWithSubject>): List<GradeWithSubject> {
		return coroutineScope {
			val newGradesFound: MutableList<GradeWithSubject> = mutableListOf()
			for (gradeEntity in gradeEntities) {
				launch {
					val gradeFound = measurePerformanceInMS(
						{ time, result ->
							Log.e(
								TAG,
								"gradeDataSource.getById: $time ms\n found = $result",
							)
						}
					) {
						gradeDataSource.getById(gradeEntity.grade.gradeId) != null
					}
					Log.i(
						TAG,
						"updateDatabase: gradeFound = $gradeFound and id = ${gradeEntity.grade.gradeId}"
					)
					if (gradeFound) {
						update(gradeEntity.grade)
					} else {
						create(gradeEntity.grade)
						newGradesFound.add(gradeEntity)
						Log.i(TAG, "updateDatabase: FOUND NEW GRADE ${gradeEntity.grade}")
					}
				}
			}
			newGradesFound
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
	
	private suspend fun DayGradeDto.toGradeWithSubject(): GradeWithSubject {
		try {
			val subjectEntity =
				subjectDataSource.getByName(subjectAncestorName)
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			// TODO: add prompt to create subject with such name, or create it forcibly
			return GradeWithSubject(
				grade = GradeEntity(
					mark = mark,
					date = date,
					fetchDateTime = LocalDateTime.now(),
					subjectMasterId = subjectEntity.subjectId,
					typeOfWork = typeOfWork,
					index = index,
					lessonIndex = lessonIndex,
					gradeId = generateGradeId(date = date, index = index, lessonIndex = lessonIndex)
				),
				subject = subjectEntity
			)
			
		} catch (e: NoSuchElementException) {
			throw RuntimeException("Failed to convert network to local", e)
		}
	}
	
	private suspend fun List<DayGradeDto>.toGradesWithSubject() = map { it.toGradeWithSubject() }
	
	private fun generateGradeId(date: LocalDate, index: Int, lessonIndex: Int): String {
		val dateStamp = date.atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond().toString()
		val gradeIndex = index.toString()
		val lessonIndexString = lessonIndex.toString()
		return (dateStamp + "_" + gradeIndex + "_" + lessonIndexString)
	}
}