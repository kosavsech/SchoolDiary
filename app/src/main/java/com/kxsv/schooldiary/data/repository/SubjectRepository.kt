package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithGrades
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithTeachers
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
	
	fun observeAll(): Flow<List<SubjectEntity>>
	
	fun getSubjectStream(subjectId: Long): Flow<SubjectEntity>
	
	fun observeSubjectWithTeachers(subjectId: Long): Flow<SubjectWithTeachers?>
	
	fun getSubjectWithGradesStream(subjectId: Long): Flow<SubjectWithGrades>
	
	suspend fun fetchSubjectNames(): MutableList<String>
	
	suspend fun getSubjects(): List<SubjectEntity>
	
	suspend fun getSubject(subjectId: Long): SubjectEntity?
	
	suspend fun getSubjectByName(subjectName: String): SubjectEntity?
	
	suspend fun getSubjectIdByName(subjectName: String): Long?
	
	suspend fun getSubjectWithTeachers(subjectId: Long): SubjectWithTeachers?
	
	suspend fun createSubject(subject: SubjectEntity, teachersIds: Set<String>): Long
	
	suspend fun updateSubject(subject: SubjectEntity, teachersIds: Set<String>?)
	
	suspend fun deleteAllSubjects()
	
	suspend fun deleteSubject(subjectId: Long)
}