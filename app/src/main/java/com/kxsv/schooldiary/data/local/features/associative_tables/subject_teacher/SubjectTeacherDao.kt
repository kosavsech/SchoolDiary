package com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher

import androidx.room.*

@Dao
interface SubjectTeacherDao {
	
	@Upsert
	suspend fun upsert(subjectTeacher: SubjectTeacher)
	
	@Query("DELETE FROM SubjectTeacher")
	suspend fun deleteAll()
	
	@Query("DELETE FROM SubjectTeacher WHERE subjectId = :subjectId")
	suspend fun deleteBySubjectId(subjectId: String)
	
	@Query("DELETE FROM SubjectTeacher WHERE teacherId = :teacherId")
	suspend fun deleteByTeacherId(teacherId: String)
}