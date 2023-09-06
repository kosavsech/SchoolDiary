package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithGrades
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithTeachers
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
	
	fun observeAll(): Flow<List<SubjectEntity>>
	
	fun getSubjectStream(subjectId: String): Flow<SubjectEntity>
	
	fun observeSubjectWithTeachers(subjectId: String): Flow<SubjectWithTeachers?>
	
	fun getSubjectWithGradesStream(subjectId: String): Flow<SubjectWithGrades>
	
	suspend fun fetchSubjectNames(): List<String>
	
	suspend fun getAll(): List<SubjectEntity>
	
	suspend fun getSubject(subjectId: String): SubjectEntity?
	
	suspend fun getSubjectByName(subjectName: String): SubjectEntity?
	
	suspend fun getSubjectIdByName(subjectName: String): String?
	
	suspend fun getSubjectWithTeachers(subjectId: String): SubjectWithTeachers?
	
	suspend fun createSubject(subject: SubjectEntity, teachersIds: Set<String>?): String
	
	suspend fun updateSubject(subject: SubjectEntity, teachersIds: Set<String>?)
	
	suspend fun deleteAllSubjects()
	
	suspend fun deleteSubject(subjectId: String)
}