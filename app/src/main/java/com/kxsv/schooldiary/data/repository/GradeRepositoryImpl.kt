package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.grade.GradeDao
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.dtos.DayGradeDto
import com.kxsv.schooldiary.data.remote.dtos.TeacherDto
import com.kxsv.schooldiary.data.remote.parsers.DayGradeParser
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateGradeId
import com.kxsv.schooldiary.data.util.remote.NetworkException
import com.kxsv.schooldiary.di.util.IoDispatcher
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.toList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
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
	
	override fun getGradesBySubjectIdStream(subjectId: String): Flow<List<GradeEntity>> {
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
	override suspend fun fetchGradesByDate(localDate: LocalDate): Pair<Map<TeacherDto, Set<String>>, List<DayGradeDto>> {
		val dayInfo = webService.getDayInfo(localDate)
		return DayGradeParser().parse(dayInfo, localDate)
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	override suspend fun fetchRecentGradesWithTeachers(): Pair<MutableList<DayGradeDto>, MutableMap<TeacherDto, MutableSet<String>>> {
		return withContext(ioDispatcher) {
			withTimeout(15000L) {
				val fetchedGrades: MutableList<DayGradeDto> = mutableListOf()
				val fetchedTeachers: MutableMap<TeacherDto, MutableSet<String>> = mutableMapOf()
				
				val startRange = Utils.currentDate
				(startRange.minusDays(14)..startRange).toList().forEach { date ->
					if (date.dayOfWeek == DayOfWeek.SUNDAY) return@forEach
					val teachersAndGradesWithSubject = async { fetchGradesByDate(date) }.await()
					teachersAndGradesWithSubject.first.keys.forEach {
						val subjectNames = fetchedTeachers.getOrDefault(it, mutableSetOf())
						subjectNames.addAll(teachersAndGradesWithSubject.first.getValue(it))
						fetchedTeachers[it] = subjectNames
					}
					
					val gradesWithSubject = teachersAndGradesWithSubject.second
					
					fetchedGrades.addAll(gradesWithSubject)
				}
				Pair(fetchedGrades, fetchedTeachers)
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
}