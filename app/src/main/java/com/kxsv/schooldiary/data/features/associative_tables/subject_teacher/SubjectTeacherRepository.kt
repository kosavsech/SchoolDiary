package com.kxsv.schooldiary.data.features.associative_tables.subject_teacher

interface SubjectTeacherRepository {

    suspend fun upsertSubjectTeacher(subjectTeacher: SubjectTeacher)

    suspend fun deleteAllSubjectTeachers()

    suspend fun deleteBySubjectId(subjectId: Long)

    suspend fun deleteByTeacherId(teacherId: Int)

}