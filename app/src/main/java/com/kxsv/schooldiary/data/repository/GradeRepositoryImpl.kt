package com.kxsv.schooldiary.data.repository

import android.util.Log
import com.kxsv.schooldiary.data.DataUtils.generateGradeId
import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.mapper.toGradesWithSubject
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
						val gradesWithSubject = fetchGradeByDate(date)
							.toGradesWithSubject(subjectDataSource = subjectDataSource)
						newGradesFound.addAll(updateDatabase(gradesWithSubject))
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
		val gradeId = generateGradeId(
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
	
	private suspend fun updateDatabase(fetchedGradeEntities: List<GradeWithSubject>): List<GradeWithSubject> {
		return coroutineScope {
			val newGradesFound: MutableList<GradeWithSubject> = mutableListOf()
			for (fetchedGradeEntity in fetchedGradeEntities) {
				launch(ioDispatcher) {
					val isGradeExisted = measurePerformanceInMS(
						{ time, result ->
							Log.e(
								TAG,
								"gradeDataSource.getById: $time ms\n found = $result",
							)
						}
					) {
						gradeDataSource.getById(fetchedGradeEntity.grade.gradeId) != null
					}
					gradeDataSource.upsert(fetchedGradeEntity.grade)
					if (!isGradeExisted) {
						newGradesFound.add(fetchedGradeEntity)
						Log.i(TAG, "updateDatabase: FOUND NEW GRADE:\n${fetchedGradeEntity.grade}")
					}
				}
			}
			newGradesFound
		}
	}
}