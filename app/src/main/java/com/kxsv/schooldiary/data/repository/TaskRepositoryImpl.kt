package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.lesson.LessonDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskDao
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.mapper.toSubjectEntitiesIndexed
import com.kxsv.schooldiary.data.mapper.toTasksWithSubject
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.dtos.TaskDto
import com.kxsv.schooldiary.data.remote.parsers.LessonParser
import com.kxsv.schooldiary.data.remote.parsers.TaskParser
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateTaskId
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.ApplicationScope
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.toList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.jsoup.select.Elements
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "TaskRepositoryImpl"

@Singleton
class TaskRepositoryImpl @Inject constructor(
	private val taskDataSource: TaskDao,
	private val webService: WebService,
	private val subjectDataSource: SubjectDao,
	private val studyDayDataSource: StudyDayDao,
	private val lessonDataSource: LessonDao,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
	@ApplicationScope private val scope: CoroutineScope,
) : TaskRepository {
	
	override fun observeAll(): Flow<List<TaskEntity>> {
		return taskDataSource.observeAll()
	}
	
	override fun observeAllWithSubject(): Flow<List<TaskWithSubject>> {
		return taskDataSource.observeAllWithSubject()
	}
	
	override fun observeAllWithSubjectForDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Flow<List<TaskWithSubject>> {
		return taskDataSource.observeAllWithSubjectForDateRange(startRange, endRange)
	}
	
	override fun observeTask(taskId: String): Flow<TaskEntity> {
		return taskDataSource.observeById(taskId)
	}
	
	override fun observeTaskWithSubject(taskId: String): Flow<TaskWithSubject> {
		return taskDataSource.observeByIdWithSubject(taskId)
	}
	
	override suspend fun getTasks(): List<TaskEntity> {
		return taskDataSource.getAll()
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 * @throws TimeoutCancellationException
	 * @return List of new tasks, which were not cached before
	 */
	override suspend fun fetchSoonTasks(): List<TaskWithSubject> {
		return withContext(ioDispatcher) {
			withTimeout(15000L) {
				val result = mutableListOf<TaskWithSubject>()
				val startRange = Utils.currentDate
				val endRange = startRange.plusDays(7)
				
				(startRange..endRange).toList().forEach { date ->
					if (date.dayOfWeek == DayOfWeek.SUNDAY) return@forEach
					@Suppress("DeferredResultUnused")
					async {
						result.addAll(fetchTasksOnDate(date = date))
					}
				}
				
				return@withTimeout result
			}
		}
	}
	
	private suspend fun fetchTasksOnDate(date: LocalDate): MutableList<TaskWithSubject> {
		return withContext(ioDispatcher) {
			val dateNewTasks: MutableList<TaskWithSubject> = mutableListOf()
			val localSchedule = async { lessonDataSource.getAllWithSubjectByDate(date) }
			val dayInfo = async { webService.getDayInfo(date) }
			
			val subjectsIndexed = if (localSchedule.await().isNotEmpty()) {
				val localClassesMapped = mutableMapOf<Int, SubjectEntity>()
				localSchedule.await().forEach {
					localClassesMapped[it.lesson.index] = it.subject
				}
				localClassesMapped
			} else {
				LessonParser()
					.parse(dayInfo = dayInfo.await(), localDate = date)
					.toSubjectEntitiesIndexed(subjectDataSource, studyDayDataSource)
			}
			
			subjectsIndexed.forEach { subjectIndexed ->
				dateNewTasks.addAll(
					fetchTasksForSubject(
						subjectIndexed = subjectIndexed,
						dayInfo = dayInfo.await(),
						date = date
					)
				)
			}
			return@withContext dateNewTasks
		}
	}
	
	/**
	 * Fetches tasks for subject and caches them
	 *
	 * @param subjectIndexed
	 * @param dayInfo
	 * @param date
	 * @return list of new tasks
	 */
	private suspend fun fetchTasksForSubject(
		subjectIndexed: Map.Entry<Int, SubjectEntity>,
		dayInfo: Elements,
		date: LocalDate,
	): List<TaskWithSubject> = withContext(ioDispatcher) {
		val netTaskVariants = TaskParser()
			.parse(dayInfo = dayInfo, date = date, subject = subjectIndexed.value)
		
		if (netTaskVariants.isEmpty()) return@withContext emptyList<TaskWithSubject>()
		val localTasks = getByDateAndSubject(date, subjectIndexed.value.subjectId)
		
		val fetchedTasksWithSubjects = netTaskVariants.toTasksWithSubject()
		
		if (localTasks.isEmpty()) {
			taskDataSource.upsertAll(fetchedTasksWithSubjects.map { it.taskEntity })
			return@withContext fetchedTasksWithSubjects
		}
		
		val newTasksForSubject = mutableListOf<TaskWithSubject>()
		val localTasksAssociated = localTasks.associateBy { it.taskId }
		localTasks.forEach { localTask ->
			if (localTask.taskId.split("_")[0].toInt() >= 100) return@forEach
			val relevantFetchedTask = fetchedTasksWithSubjects.run {
				val index = this
					.map { it.taskEntity }
					.sortedBy { it.taskId }
					.binarySearchBy(localTask.taskId) { it.taskId }
				if (index != -1) {
					return@run this[index]
				} else {
					return@run null
				}
			}
			if (relevantFetchedTask == null || !localTask.isContentEqual(relevantFetchedTask.taskEntity)) {
				taskDataSource.deleteById(localTask.taskId)
			}
		}
		fetchedTasksWithSubjects.forEach { fetchedTask ->
			val wereFetchedBefore = localTasksAssociated.containsKey(fetchedTask.taskEntity.taskId)
			
			if (!wereFetchedBefore) {
				newTasksForSubject.add(fetchedTask)
				taskDataSource.upsert(fetchedTask.taskEntity)
			}
		}
		return@withContext newTasksForSubject
	}
	
	private fun TaskEntity.isContentEqual(localTask: TaskEntity): Boolean {
		if (this.taskId != localTask.taskId) return false
		if (this.title != localTask.title && this.title != localTask.fetchedTitleBoundToId) {
			return false
		}
		return true
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	override suspend fun fetchTaskVariantsForSubjectByDate(
		date: LocalDate,
		subject: SubjectEntity,
	): List<TaskDto> {
		val dayInfo = webService.getDayInfo(date)
		return TaskParser().parse(
			dayInfo = dayInfo,
			date = date,
			subject = subject,
		)
	}
	
	override suspend fun getById(taskId: String): TaskEntity? {
		return taskDataSource.getById(taskId)
	}
	
	override suspend fun getByDateAndSubject(date: LocalDate, subjectId: String): List<TaskEntity> {
		return taskDataSource.getByDateAndSubject(date = date, subjectId = subjectId)
	}
	
	override suspend fun getTaskWithSubject(taskId: String): TaskWithSubject? {
		return taskDataSource.getByIdWithSubject(taskId)
	}
	
	override suspend fun createTask(task: TaskEntity, fetchedLessonIndex: Int?): String {
		if (task.subjectMasterId == null) throw RuntimeException("Shouldn't be called with null subjectMasterId")
		val localManualTasks = taskDataSource
			.getByDateAndSubject(task.dueDate, task.subjectMasterId)
			.filter { !it.isFetched }
		
		val lessonIndex = fetchedLessonIndex
			?: if (localManualTasks.isEmpty()) { // first local(user entered title by hand) task on this day
				100
			} else {
				localManualTasks.maxOf { it.taskId.split("_")[0].toInt() } + 1
			}
		val taskId = generateTaskId(
			dueDate = task.dueDate,
			subjectId = task.subjectMasterId,
			lessonIndex = lessonIndex,
		)
		taskDataSource.insert(task.copy(taskId = taskId))
		return taskId
	}
	
	override suspend fun updateTask(task: TaskEntity) {
		taskDataSource.upsert(task)
	}
	
	override suspend fun deleteAllTasks() {
		taskDataSource.deleteAll()
	}
	
	override suspend fun deleteTask(taskId: String) {
		taskDataSource.deleteById(taskId)
	}
	
}