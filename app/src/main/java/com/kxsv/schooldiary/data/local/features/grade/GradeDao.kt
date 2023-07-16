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
	
	@Query("SELECT * FROM Grade")
	suspend fun getAll(): List<Grade>
	
	@Query("SELECT * FROM Grade WHERE gradeId = :gradeId")
	suspend fun getById(gradeId: Long): Grade?
	
	@Upsert
	suspend fun upsertAll(grades: List<Grade>)
	
	@Upsert
	suspend fun upsert(grade: Grade): Long
	
	@Query("DELETE FROM Grade")
	suspend fun deleteAll()
	
	@Query("DELETE FROM Grade WHERE gradeId = :gradeId")
	suspend fun deleteById(gradeId: Long)
}