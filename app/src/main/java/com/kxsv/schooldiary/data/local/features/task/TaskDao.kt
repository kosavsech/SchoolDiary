package com.kxsv.schooldiary.data.local.features.task

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.TASK_TABLE_NAME
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
	
	@Query("SELECT * FROM $TASK_TABLE_NAME")
	fun observeAll(): Flow<List<TaskEntity>>
	
	@Transaction
	@Query("SELECT * FROM $TASK_TABLE_NAME")
	fun observeAllWithSubject(): Flow<List<TaskWithSubject>>
	
	@Transaction
	@Query(
		"SELECT * FROM $TASK_TABLE_NAME WHERE " +
				"dueDate >= :startRange AND dueDate <= :endRange"
	)
	fun observeAllWithSubjectForDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Flow<List<TaskWithSubject>>
	
	@Query("SELECT * FROM $TASK_TABLE_NAME WHERE taskId = :taskId")
	fun observeById(taskId: String): Flow<TaskEntity>
	
	@Transaction
	@Query("SELECT * FROM $TASK_TABLE_NAME WHERE taskId = :taskId")
	fun observeByIdWithSubject(taskId: String): Flow<TaskWithSubject>
	
	@Query("SELECT * FROM $TASK_TABLE_NAME")
	suspend fun getAll(): List<TaskEntity>
	
	@Query("SELECT * FROM $TASK_TABLE_NAME WHERE taskId = :taskId")
	suspend fun getById(taskId: String): TaskEntity?
	
	@Query("SELECT * FROM $TASK_TABLE_NAME WHERE dueDate = :date AND subjectMasterId = :subjectId")
	suspend fun getByDateAndSubject(date: LocalDate, subjectId: String): List<TaskEntity>
	
	@Transaction
	@Query("SELECT * FROM $TASK_TABLE_NAME WHERE taskId = :taskId")
	suspend fun getByIdWithSubject(taskId: String): TaskWithSubject?
	
	@Upsert
	suspend fun upsertAll(tasks: List<TaskEntity>)
	
	@Upsert
	suspend fun upsert(task: TaskEntity)
	
	@Insert
	suspend fun insert(task: TaskEntity)
	
	@Query("DELETE FROM $TASK_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $TASK_TABLE_NAME WHERE taskId = :taskId")
	suspend fun deleteById(taskId: String)
}