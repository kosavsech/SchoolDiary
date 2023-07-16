package com.kxsv.schooldiary.data.local.features.subject

import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.local.features.teacher.Teacher
import com.kxsv.schooldiary.domain.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
	private val subjectDataSource: SubjectDao,
	private val subjectTeacherDataSource: SubjectTeacherDao,
	//@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : SubjectRepository {
	
	override fun getSubjectsStream(): Flow<List<Subject>> {
		return subjectDataSource.observeAll()
	}
	
	override fun getSubjectStream(subjectId: Long): Flow<Subject> {
		return subjectDataSource.observeById(subjectId)
	}
	
	override fun getSubjectWithGradesStream(subjectId: Long): Flow<SubjectWithGrades> {
		return subjectDataSource.observeByIdWithGrades(subjectId)
	}
	
	override suspend fun getSubjects(): List<Subject> {
		return subjectDataSource.getAll()
	}
	
	override suspend fun getSubject(subjectId: Long): Subject? {
		return subjectDataSource.getById(subjectId)
	}
	
	override suspend fun getSubjectByName(subjectName: String): Subject? {
		return subjectDataSource.getByName(subjectName)
	}
	
	override suspend fun getSubjectWithTeachers(subjectId: Long): SubjectWithTeachers? {
		return subjectDataSource.getByIdWithTeachers(subjectId)
	}
	
	override suspend fun createSubject(subject: Subject, teachers: Set<Teacher>) {
		val subjectId = subjectDataSource.upsert(subject)
		
		teachers.forEach {
			subjectTeacherDataSource.upsert(
				SubjectTeacher(
					subjectId = subjectId,
					teacherId = it.teacherId
				)
			)
		}
	}
	
	override suspend fun updateSubject(subject: Subject, teachers: Set<Teacher>) {
		subjectDataSource.upsert(subject)
		
		subjectTeacherDataSource.deleteBySubjectId(subject.subjectId)
		teachers.forEach { teacher ->
			subjectTeacherDataSource.upsert(
				SubjectTeacher(subject.subjectId, teacher.teacherId)
			)
		}
	}
	
	override suspend fun deleteAllSubjects() {
		subjectDataSource.deleteAll()
	}
	
	override suspend fun deleteSubject(subjectId: Long) {
		subjectDataSource.deleteById(subjectId)
	}
	
}