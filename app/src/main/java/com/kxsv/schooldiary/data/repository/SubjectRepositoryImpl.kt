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
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
	private val subjectDataSource: SubjectDao,
	private val subjectTeacherDataSource: SubjectTeacherDao,
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
	
	override suspend fun fetchSubjectNames(): MutableList<String> {
		val termYearRows = webService.getTermEduPerformance(term = EduPerformancePeriod.YEAR.value)
		return SubjectParser().parse(termYearRows)
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