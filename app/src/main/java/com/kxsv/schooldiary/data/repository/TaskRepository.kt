package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.remote.dtos.TaskDto
import com.kxsv.schooldiary.data.remote.util.NetworkException
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {
	
	fun observeAll(): Flow<List<TaskEntity>>
	
	fun observeAllWithSubject(): Flow<List<TaskWithSubject>>
	
	fun observeAllWithSubjectForDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Flow<List<TaskWithSubject>>
	
	fun observeTask(taskId: String): Flow<TaskEntity>
	
	fun observeTaskWithSubject(taskId: String): Flow<TaskWithSubject>
	
	suspend fun getTasks(): List<TaskEntity>
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws java.io.IOException if couldn't parseTermRows document
	 * @return List of new tasks, which were not cached before
	 */
	suspend fun fetchSoonTasks(): List<TaskWithSubject>
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws java.io.IOException if couldn't parseTermRows document
	 */
	suspend fun fetchTaskVariantsForSubjectByDate(
		date: LocalDate,
		subject: SubjectEntity,
	): List<TaskDto>
	
	suspend fun getById(taskId: String): TaskEntity?
	
	suspend fun getByDateAndSubject(date: LocalDate, subjectId: String): List<TaskEntity>
	
	suspend fun getTaskWithSubject(taskId: String): TaskWithSubject?
	
	suspend fun createTask(task: TaskEntity, fetchedLessonIndex: Int?): String
	
	suspend fun updateTask(task: TaskEntity)
	
	suspend fun deleteAllTasks()
	
	suspend fun deleteTask(taskId: String)
	
}