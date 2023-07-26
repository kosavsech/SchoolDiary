package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.task.TaskDao
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
	private val taskDataSource: TaskDao,
) : TaskRepository {
	
	override fun observeAll(): Flow<List<TaskEntity>> {
		return taskDataSource.observeAll()
	}
	
	override fun observeTask(taskId: Long): Flow<TaskEntity> {
		return taskDataSource.observeById(taskId)
	}
	
	override suspend fun getTasks(): List<TaskEntity> {
		return taskDataSource.getAll()
	}
	
	override suspend fun getTask(taskId: Long): TaskEntity? {
		return taskDataSource.getById(taskId)
	}
	
	override suspend fun createTask(task: TaskEntity): Long {
		return taskDataSource.upsert(task)
	}
	
	override suspend fun updateTask(task: TaskEntity) {
		taskDataSource.upsert(task)
	}
	
	override suspend fun deleteAllTasks() {
		taskDataSource.deleteAll()
	}
	
	override suspend fun deleteTask(taskId: Long) {
		taskDataSource.deleteById(taskId)
	}
	
}