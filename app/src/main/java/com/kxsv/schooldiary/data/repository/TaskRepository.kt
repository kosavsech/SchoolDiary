package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
	
	fun observeAll(): Flow<List<TaskEntity>>
	
	fun observeTask(taskId: Long): Flow<TaskEntity>
	
	suspend fun getTasks(): List<TaskEntity>
	
	suspend fun getTask(taskId: Long): TaskEntity?
	
	suspend fun createTask(task: TaskEntity): Long
	
	suspend fun updateTask(task: TaskEntity)
	
	suspend fun deleteAllTasks()
	
	suspend fun deleteTask(taskId: Long)
}