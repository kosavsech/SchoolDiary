package com.kxsv.schooldiary.data.local.features.grade

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.GRADE_TABLE_NAME
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface GradeDao {
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME ORDER BY date DESC")
	fun observeAllOrderedByMarkDate(): Flow<List<GradeEntity>>
	
	@Transaction
	@Query("SELECT * FROM $GRADE_TABLE_NAME ORDER BY date DESC")
	fun observeAlleWithSubjectOrderedByMarkDate(): Flow<List<GradeWithSubject>>
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME ORDER BY fetchDateTime DESC")
	fun observeAllOrderedByFetchDate(): Flow<List<GradeEntity>>
	
	@Transaction
	@Query("SELECT * FROM $GRADE_TABLE_NAME ORDER BY fetchDateTime DESC")
	fun observeAlleWithSubjectOrderedByFetchDate(): Flow<List<GradeWithSubject>>
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME WHERE subjectMasterId = :subjectId ORDER BY date DESC")
	fun observeAllBySubjectId(subjectId: String): Flow<List<GradeEntity>>
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME WHERE gradeId = :gradeId")
	fun observeById(gradeId: String): Flow<GradeEntity>
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME ORDER BY date DESC")
	suspend fun getAll(): List<GradeEntity>
	
	@Transaction
	@Query("SELECT * FROM $GRADE_TABLE_NAME ORDER BY date DESC")
	suspend fun getAllWithSubjects(): List<GradeWithSubject>
	
	@Query("SELECT * FROM $GRADE_TABLE_NAME WHERE date = :date ORDER BY lessonIndex DESC")
	suspend fun getAllByDate(date: LocalDate): List<GradeEntity>
	
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