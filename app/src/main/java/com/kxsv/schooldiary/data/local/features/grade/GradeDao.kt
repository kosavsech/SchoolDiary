package com.kxsv.schooldiary.data.local.features.grade

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.GRADE_TABLE_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface GradeDao {
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME")
	fun observeAll(): Flow<List<GradeEntity>>
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME WHERE subjectMasterId = :subjectId")
	fun observeAllBySubjectId(subjectId: Long): Flow<List<GradeEntity>>
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME WHERE gradeId = :gradeId")
	fun observeById(gradeId: String): Flow<GradeEntity>
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME ORDER BY date DESC")
	suspend fun getAll(): List<GradeEntity>
	
	@Transaction
	@Query("SELECT * FROM $GRADE_TABLE_NAME ORDER BY date DESC")
	suspend fun getAllWithSubjects(): List<GradeWithSubject>
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME WHERE gradeId = :gradeId")
	suspend fun getById(gradeId: String): GradeEntity?
	
	@Transaction
	@Query("SELECT * FROM $GRADE_TABLE_NAME WHERE gradeId = :gradeId")
	suspend fun getByIdWithSubject(gradeId: String): GradeWithSubject?
	
	@Upsert
	suspend fun upsertAll(grades: List<GradeEntity>)
	
	@Upsert
	suspend fun upsert(grade: GradeEntity)
	
	@Query("DELETE FROM $GRADE_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $GRADE_TABLE_NAME WHERE gradeId = :gradeId")
	suspend fun deleteById(gradeId: String)
}