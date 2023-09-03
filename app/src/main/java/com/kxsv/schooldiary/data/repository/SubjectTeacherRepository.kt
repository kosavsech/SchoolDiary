package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher

interface SubjectTeacherRepository {
	suspend fun upsert(subjectTeacher: SubjectTeacher)
}