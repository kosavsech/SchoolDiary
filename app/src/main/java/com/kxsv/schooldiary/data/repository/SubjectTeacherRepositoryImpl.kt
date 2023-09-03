package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacherDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectTeacherRepositoryImpl @Inject constructor(
	private val subjectTeacherDataSource: SubjectTeacherDao,
) : SubjectTeacherRepository {
	
	override suspend fun upsert(subjectTeacher: SubjectTeacher) {
		subjectTeacherDataSource.upsert(subjectTeacher)
	}
	
}