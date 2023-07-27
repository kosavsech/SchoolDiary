package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithGrades
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithTeachers
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
	private val subjectDataSource: SubjectDao,
	private val subjectTeacherDataSource: SubjectTeacherDao,
	//@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : SubjectRepository {
	
	override fun observeAll(): Flow<List<SubjectEntity>> {
		return subjectDataSource.observeAll()
	}
	
	override fun getSubjectStream(subjectId: Long): Flow<SubjectEntity> {
		return subjectDataSource.observeById(subjectId)
	}
	
	override fun observeSubjectWithTeachers(subjectId: Long): Flow<SubjectWithTeachers?> {
		return subjectDataSource.observeByIdWithTeachers(subjectId)
	}
	
	override fun getSubjectWithGradesStream(subjectId: Long): Flow<SubjectWithGrades> {
		return subjectDataSource.observeByIdWithGrades(subjectId)
	}
	
	override suspend fun getSubjects(): List<SubjectEntity> {
		return subjectDataSource.getAll()
	}
	
	override suspend fun getSubject(subjectId: Long): SubjectEntity? {
		return subjectDataSource.getById(subjectId)
	}
	
	override suspend fun getSubjectByName(subjectName: String): SubjectEntity? {
		return subjectDataSource.getByName(subjectName)
	}
	
	override suspend fun getSubjectIdByName(subjectName: String): Long? {
		return subjectDataSource.getByName(subjectName)?.subjectId
	}
	
	override suspend fun getSubjectWithTeachers(subjectId: Long): SubjectWithTeachers? {
		return subjectDataSource.getByIdWithTeachers(subjectId)
	}
	
	override suspend fun createSubject(subject: SubjectEntity, teachers: Set<TeacherEntity>) {
		val subjectId = subjectDataSource.upsert(subject)
		
		teachers.forEach {
			subjectTeacherDataSource.upsert(
				SubjectTeacher(subjectId = subjectId, teacherId = it.teacherId)
			)
		}
	}
	
	override suspend fun updateSubject(subject: SubjectEntity, teachers: Set<TeacherEntity>?) {
		subjectDataSource.upsert(subject)
		
		if (teachers != null) {
			subjectTeacherDataSource.deleteBySubjectId(subject.subjectId) // TODO: is it excess?
			teachers.forEach { teacher ->
				subjectTeacherDataSource.upsert(
					SubjectTeacher(subject.subjectId, teacher.teacherId)
				)
			}
		}
	}
	
	override suspend fun deleteAllSubjects() {
		subjectDataSource.deleteAll()
	}
	
	override suspend fun deleteSubject(subjectId: Long) {
		subjectDataSource.deleteById(subjectId)
	}
	
}