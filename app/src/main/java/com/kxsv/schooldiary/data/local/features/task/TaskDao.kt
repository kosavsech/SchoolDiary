package com.kxsv.schooldiary.data.local.features.task

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
	
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME}")
	fun observeAll(): Flow<List<TaskEntity>>
	
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME} WHERE taskId = :taskId")
	fun observeById(taskId: Long): Flow<TaskEntity>
	
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME}")
	suspend fun getAll(): List<TaskEntity>
	
	@Query("SELECT * FROM ${DatabaseConstants.TASK_TABLE_NAME} WHERE taskId = :taskId")
	suspend fun getById(taskId: Long): TaskEntity?
	
	@Upsert
	suspend fun upsertAll(tasks: List<TaskEntity>)
	
	@Upsert
	suspend fun upsert(task: TaskEntity): Long
	
	@Query("DELETE FROM ${DatabaseConstants.TASK_TABLE_NAME}")
	suspend fun deleteAll()
	
	@Query("DELETE FROM ${DatabaseConstants.TASK_TABLE_NAME} WHERE taskId = :taskId")
	suspend fun deleteById(taskId: Long)
}