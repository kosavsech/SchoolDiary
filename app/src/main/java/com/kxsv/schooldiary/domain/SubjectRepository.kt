package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.local.features.subject.Subject
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithGrades
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithTeachers
import com.kxsv.schooldiary.data.local.features.teacher.Teacher
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
	
	fun getSubjectsStream(): Flow<List<Subject>>
	
	fun getSubjectStream(subjectId: Long): Flow<Subject>
	
	fun getSubjectWithGradesStream(subjectId: Long): Flow<SubjectWithGrades>
	
	suspend fun getSubjects(): List<Subject>
	
	suspend fun getSubject(subjectId: Long): Subject?
	
	suspend fun getSubjectByName(subjectName: String): Subject?
	
	suspend fun getSubjectWithTeachers(subjectId: Long): SubjectWithTeachers?
	
	suspend fun createSubject(subject: Subject, teachers: Set<Teacher>)
	
	suspend fun updateSubject(subject: Subject, teachers: Set<Teacher>)
	
	suspend fun deleteAllSubjects()
	
	suspend fun deleteSubject(subjectId: Long)
}