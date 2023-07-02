package com.kxsv.schooldiary.data.features.associative_tables.subject_teacher

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectTeacherRepositoryImpl @Inject constructor(
    private val subjectTeacherDataSource: SubjectTeacherDao,
) : SubjectTeacherRepository {

    override suspend fun upsertSubjectTeacher(subjectTeacher: SubjectTeacher) {
        subjectTeacherDataSource.upsert(subjectTeacher)
    }

    override suspend fun deleteAllSubjectTeachers() {
        subjectTeacherDataSource.deleteAll()
    }

    override suspend fun deleteBySubjectId(subjectId: Long) {
        subjectTeacherDataSource.deleteBySubjectId(subjectId)
    }

    override suspend fun deleteByTeacherId(teacherId: Int) {
        subjectTeacherDataSource.deleteByTeacherId(teacherId)
    }

}