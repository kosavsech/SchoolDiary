package com.kxsv.schooldiary.data.features.associative_tables.subject_teacher

import androidx.room.*

@Dao
interface SubjectTeacherDao {

    @Upsert
    suspend fun upsert(subjectTeacher: SubjectTeacher)

    @Query("DELETE FROM SubjectTeacher")
    suspend fun deleteAll()

    @Query("DELETE FROM SubjectTeacher WHERE subjectId = :subjectId")
    suspend fun deleteBySubjectId(subjectId: Long)

    @Query("DELETE FROM SubjectTeacher WHERE teacherId = :teacherId")
    suspend fun deleteByTeacherId(teacherId: Int)
}