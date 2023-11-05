package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithGrades
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithTeachers
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.parsers.EduPerformanceParser
import com.kxsv.schooldiary.data.remote.parsers.SubjectParser
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateSubjectId
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.user_preferences.Period
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.util.Extensions.toList
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.isHoliday
import com.kxsv.schooldiary.util.Utils.periodRangeEntryToLocalDate
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.jsoup.select.Elements
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
	private val subjectDataSource: SubjectDao,
	private val subjectTeacherDataSource: SubjectTeacherDao,
	private val userPreferencesRepository: UserPreferencesRepository,
	private val webService: WebService,
) : SubjectRepository {
	
	override fun observeAll(): Flow<List<SubjectEntity>> {
		return subjectDataSource.observeAll()
	}
	
	override fun getSubjectStream(subjectId: String): Flow<SubjectEntity> {
		return subjectDataSource.observeById(subjectId)
	}
	
	override fun observeSubjectWithTeachers(subjectId: String): Flow<SubjectWithTeachers?> {
		return subjectDataSource.observeByIdWithTeachers(subjectId)
	}
	
	override fun getSubjectWithGradesStream(subjectId: String): Flow<SubjectWithGrades> {
		return subjectDataSource.observeByIdWithGrades(subjectId)
	}
	
	override suspend fun fetchSubjectNames(): Set<String> = coroutineScope {
		val startRange = Utils.currentDate.minusDays(7)
		val endRange = Utils.currentDate.plusDays(7)
		
		val periodType = userPreferencesRepository.getEducationPeriodType()
		val termsPeriodsRanges = userPreferencesRepository.getPeriodsRanges()
			.filter { Period.getTypeByPeriod(it.period) == PeriodType.TERMS }
			.map {
				periodRangeEntryToLocalDate(it.range.start)..periodRangeEntryToLocalDate(it.range.end)
			}
		
		val dayInfos = mutableListOf<Deferred<Elements>>()
		(startRange..endRange).toList().forEach { date ->
			if (isHoliday(date, termsPeriodsRanges)) return@forEach
			dayInfos += async { webService.getDayInfo(date) }
		}
		val termIndexRange = if (periodType == PeriodType.TERMS) {
			0..3
		} else {
			0..1
		}.toMutableList()
		termIndexRange.add(4)
		val subjectsFromReportCard = mutableListOf<Deferred<Elements>>()
		for (termIndex in termIndexRange) {
			subjectsFromReportCard += async {
				val term = EduPerformancePeriod.values()[termIndex]
				webService.getTermEduPerformance(term = term)
			}
		}
		val result = SubjectParser().parseDays(dayInfos.awaitAll())
		result.addAll(EduPerformanceParser().parseNames(subjectsFromReportCard.awaitAll()))
		return@coroutineScope result
	}
	
	
	override suspend fun getAll(): List<SubjectEntity> {
		return subjectDataSource.getAll()
	}
	
	override suspend fun getSubject(subjectId: String): SubjectEntity? {
		return subjectDataSource.getById(subjectId)
	}
	
	override suspend fun getSubjectByName(subjectName: String): SubjectEntity? {
		return subjectDataSource.getByName(subjectName)
	}
	
	override suspend fun getSubjectIdByName(subjectName: String): String? {
		return subjectDataSource.getByName(subjectName)?.subjectId
	}
	
	override suspend fun getSubjectWithTeachers(subjectId: String): SubjectWithTeachers? {
		return subjectDataSource.getByIdWithTeachers(subjectId)
	}
	
	override suspend fun createSubject(subject: SubjectEntity, teachersIds: Set<String>?): String {
		val subjectId = generateSubjectId(subject.fullName)
		subjectDataSource.upsert(subject.copy(subjectId = subjectId))
		
		teachersIds?.forEach { teacherId ->
			subjectTeacherDataSource.upsert(
				SubjectTeacher(subjectId = subjectId, teacherId = teacherId)
			)
		}
		return subjectId
	}
	
	override suspend fun updateSubject(subject: SubjectEntity, teachersIds: Set<String>?) {
		subjectDataSource.update(subject)
		
		if (teachersIds != null) {
			subjectTeacherDataSource.deleteBySubjectId(subject.subjectId)
			teachersIds.forEach { teacherId ->
				subjectTeacherDataSource.upsert(
					SubjectTeacher(subject.subjectId, teacherId)
				)
			}
		}
	}
	
	override suspend fun deleteAllSubjects() {
		subjectDataSource.deleteAll()
	}
	
	override suspend fun deleteSubject(subjectId: String) {
		subjectDataSource.deleteById(subjectId)
	}
	
}