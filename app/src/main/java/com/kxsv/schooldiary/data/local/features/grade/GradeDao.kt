package com.kxsv.schooldiary.data.local.features.grade

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
	
	@Query("SELECT * FROM Grade")
	fun observeAll(): Flow<List<Grade>>
	
	@Query("SELECT * FROM Grade WHERE subjectMasterId = :subjectId")
	fun observeAllBySubjectId(subjectId: Long): Flow<List<Grade>>
	
	@Query("SELECT * FROM Grade WHERE gradeId = :gradeId")
	fun observeById(gradeId: Long): Flow<Grade>
	
	@Query("SELECT * FROM Grade ORDER BY date DESC")
	suspend fun getAll(): List<Grade>
	
	@Transaction
	@Query("SELECT * FROM Grade ORDER BY date DESC")
	suspend fun getAllWithSubjects(): List<GradeWithSubject>
	
	@Query("SELECT * FROM Grade WHERE gradeId = :gradeId")
	suspend fun getById(gradeId: Long): Grade?
	
	@Transaction
	@Query("SELECT * FROM Grade WHERE gradeId = :gradeId")
	suspend fun getByIdWithSubject(gradeId: Long): GradeWithSubject?
	
	@Upsert
	suspend fun upsertAll(grades: List<Grade>)
	
	@Upsert
	suspend fun upsert(grade: Grade): Long
	
	@Query("DELETE FROM Grade")
	suspend fun deleteAll()
	
	@Query("DELETE FROM Grade WHERE gradeId = :gradeId")
	suspend fun deleteById(gradeId: Long)
}