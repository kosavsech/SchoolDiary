package com.kxsv.schooldiary.data.local.features.subject

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.SUBJECT_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME")
	fun observeAll(): Flow<List<SubjectEntity>>
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	fun observeById(subjectId: Long): Flow<SubjectEntity>
	
	@Transaction
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	fun observeByIdWithGrades(subjectId: Long): Flow<SubjectWithGrades>
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME")
	suspend fun getAll(): List<SubjectEntity>
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	suspend fun getById(subjectId: Long): SubjectEntity?
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE fullName = :subjectName")
	suspend fun getByName(subjectName: String): SubjectEntity?
	
	@Transaction
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	suspend fun getByIdWithTeachers(subjectId: Long): SubjectWithTeachers?
	
	@Upsert
	suspend fun upsertAll(subjects: List<SubjectEntity>)
	
	@Upsert
	suspend fun upsert(subject: SubjectEntity): Long
	
	@Query("DELETE FROM $SUBJECT_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	suspend fun deleteById(subjectId: Long): Int
}