package com.kxsv.schooldiary.data.local.features.grade

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
	
	@Query("SELECT * FROM GradeEntity")
	fun observeAll(): Flow<List<GradeEntity>>
	
	@Query("SELECT * FROM GradeEntity WHERE subjectMasterId = :subjectId")
	fun observeAllBySubjectId(subjectId: Long): Flow<List<GradeEntity>>
	
	@Query("SELECT * FROM GradeEntity WHERE gradeId = :gradeId")
	fun observeById(gradeId: String): Flow<GradeEntity>
	
	@Query("SELECT * FROM GradeEntity ORDER BY date DESC")
	suspend fun getAll(): List<GradeEntity>
	
	@Transaction
	@Query("SELECT * FROM GradeEntity ORDER BY date DESC")
	suspend fun getAllWithSubjects(): List<GradeWithSubject>
	
	@Query("SELECT * FROM GradeEntity WHERE gradeId = :gradeId")
	suspend fun getById(gradeId: String): GradeEntity?
	
	@Transaction
	@Query("SELECT * FROM GradeEntity WHERE gradeId = :gradeId")
	suspend fun getByIdWithSubject(gradeId: String): GradeWithSubject?
	
	@Upsert
	suspend fun upsertAll(grades: List<GradeEntity>)
	
	@Upsert
	suspend fun upsert(grade: GradeEntity)
	
	@Query("DELETE FROM GradeEntity")
	suspend fun deleteAll()
	
	@Query("DELETE FROM GradeEntity WHERE gradeId = :gradeId")
	suspend fun deleteById(gradeId: String)
}