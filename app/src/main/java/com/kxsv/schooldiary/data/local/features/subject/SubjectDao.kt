package com.kxsv.schooldiary.data.local.features.subject

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.SUBJECT_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME")
	fun observeAll(): Flow<List<SubjectEntity>>
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	fun observeById(subjectId: String): Flow<SubjectEntity>
	
	@Transaction
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	fun observeByIdWithTeachers(subjectId: String): Flow<SubjectWithTeachers>
	
	@Transaction
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	fun observeByIdWithGrades(subjectId: String): Flow<SubjectWithGrades>
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME ORDER BY displayName")
	suspend fun getAll(): List<SubjectEntity>
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	suspend fun getById(subjectId: String): SubjectEntity?
	
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE fullName = :fullSubjectName")
	suspend fun getByName(fullSubjectName: String): SubjectEntity?
	
	@Transaction
	@Query("SELECT * FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	suspend fun getByIdWithTeachers(subjectId: String): SubjectWithTeachers?
	
	@Upsert
	suspend fun upsertAll(subjects: List<SubjectEntity>)
	
	@Upsert
	suspend fun upsert(subject: SubjectEntity): Long
	
	@Update
	suspend fun update(subject: SubjectEntity)
	
	@Query("DELETE FROM $SUBJECT_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $SUBJECT_TABLE_NAME WHERE subjectId = :subjectId")
	suspend fun deleteById(subjectId: String): Int
}