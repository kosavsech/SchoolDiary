package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithGrades
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithTeachers
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.parsers.SubjectParser
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateSubjectId
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.toList
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import org.jsoup.select.Elements
import java.time.DayOfWeek
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
	
	override suspend fun fetchSubjectNames(): List<String> = coroutineScope {
		val startRange = Utils.currentDate
		val endRange = startRange.plusDays(14)
		val dayInfos = mutableListOf<Deferred<Elements>>()
		
		(startRange..endRange).toList().forEach { date ->
			if (date.dayOfWeek == DayOfWeek.SUNDAY) return@forEach
			dayInfos.add(
				async { webService.getDayInfo(date) }
			)
		}
		
		return@coroutineScope SubjectParser().parseDays(dayInfos.awaitAll())
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