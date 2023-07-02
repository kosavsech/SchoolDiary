package com.kxsv.schooldiary.data.features.subjects

import com.kxsv.schooldiary.data.features.teachers.Teacher
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {

    fun getSubjectsStream(): Flow<List<Subject>>

    fun getSubjectStream(subjectId: Long): Flow<Subject>

    suspend fun getSubjects(): List<Subject>

    suspend fun getSubject(subjectId: Long): Subject?

    suspend fun getSubjectWithTeachers(subjectId: Long): SubjectWithTeachers?

    suspend fun createSubject(subject: Subject, teachers: Set<Teacher>)

    suspend fun updateSubject(subject: Subject, teachers: Set<Teacher>)

    suspend fun deleteAllSubjects()

    suspend fun deleteSubject(subjectId: Long)
}