package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskDao
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.task.TaskDto
import com.kxsv.schooldiary.data.remote.task.TaskParser
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
	private val taskDataSource: TaskDao,
	private val webService: WebService,
) : TaskRepository {
	
	override fun observeAll(): Flow<List<TaskEntity>> {
		return taskDataSource.observeAll()
	}
	
	override fun observeAllWithSubject(): Flow<List<TaskWithSubject>> {
		return taskDataSource.observeAllWithSubject()
	}
	
	override fun observeTask(taskId: Long): Flow<TaskEntity> {
		return taskDataSource.observeById(taskId)
	}
	
	override fun observeTaskWithSubject(taskId: Long): Flow<TaskWithSubject> {
		return taskDataSource.observeByIdWithSubject(taskId)
	}
	
	override suspend fun getTasks(): List<TaskEntity> {
		return taskDataSource.getAll()
	}
	
	override suspend fun fetchTask(date: LocalDate, subject: SubjectEntity): List<TaskDto> {
		val dayInfo = webService.getDayInfo(date)
		return TaskParser().parse(dayInfo = dayInfo, date = date, subject = subject)
	}
	
	override suspend fun getTask(taskId: Long): TaskEntity? {
		return taskDataSource.getById(taskId)
	}
	
	override suspend fun getTaskWithSubject(taskId: Long): TaskWithSubject? {
		return taskDataSource.getByIdWithSubject(taskId)
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