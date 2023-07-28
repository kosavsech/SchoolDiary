package com.kxsv.schooldiary.data.local.features.task

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TaskDao {
	
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME}")
	fun observeAll(): Flow<List<TaskEntity>>
	
	@Transaction
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME}")
	fun observeAllWithSubject(): Flow<List<TaskWithSubject>>
	
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME} WHERE taskId = :taskId")
	fun observeById(taskId: Long): Flow<TaskEntity>
	
	@Transaction
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME} WHERE taskId = :taskId")
	fun observeByIdWithSubject(taskId: Long): Flow<TaskWithSubject>
	
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME}")
	suspend fun getAll(): List<TaskEntity>
	
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME} WHERE taskId = :taskId")
	suspend fun getById(taskId: Long): TaskEntity?
	
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME} WHERE dueDate = :date AND subjectMasterId = :subjectId")
	suspend fun getByDateAndSubject(date: LocalDate, subjectId: Long): List<TaskEntity>
	
	@Transaction
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME} WHERE taskId = :taskId")
	suspend fun getByIdWithSubject(taskId: Long): TaskWithSubject?
	
	@Upsert
	suspend fun upsertAll(tasks: List<TaskEntity>)
	
	@Upsert
	suspend fun upsert(task: TaskEntity): Long
	
	@Query("DELETE FROM ${DatabaseConstants.TASK_TABLE_NAME}")
	suspend fun deleteAll()
	
	@Query("DELETE FROM ${DatabaseConstants.TASK_TABLE_NAME} WHERE taskId = :taskId")
	suspend fun deleteById(taskId: Long)
}