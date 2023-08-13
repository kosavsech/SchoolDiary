package com.kxsv.schooldiary.data.repository

import android.util.Log
import com.kxsv.schooldiary.data.local.features.lesson.LessonDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskAndUniqueIdWithSubject
import com.kxsv.schooldiary.data.local.features.task.TaskDao
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.mapper.toSubjectEntitiesIndexed
import com.kxsv.schooldiary.data.mapper.toTaskEntities
import com.kxsv.schooldiary.data.mapper.toTasksAndUniqueIdWithSubject
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.dtos.TaskDto
import com.kxsv.schooldiary.data.remote.parsers.LessonParser
import com.kxsv.schooldiary.data.remote.parsers.TaskParser
import com.kxsv.schooldiary.data.util.remote.NetworkException
import com.kxsv.schooldiary.di.util.ApplicationScope
import com.kxsv.schooldiary.di.util.IoDispatcher
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
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
	
	override fun observeTask(taskId: Long): Flow<TaskEntity> {
		return taskDataSource.observeById(taskId)
	}
	
	override fun observeTaskWithSubject(taskId: Long): Flow<TaskWithSubject> {
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
	override suspend fun fetchSoonTasks(): List<TaskAndUniqueIdWithSubject> {
		return withContext(ioDispatcher) {
			withTimeout(15000L) {
				val startRange = Utils.currentDate
				val endRange = startRange.plusDays(7)
				val result: MutableList<TaskAndUniqueIdWithSubject> = mutableListOf()
				
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
	
	private suspend fun fetchTasksOnDate(date: LocalDate): MutableList<TaskAndUniqueIdWithSubject> {
		return withContext(ioDispatcher) {
			val dateNewTasks: MutableList<TaskAndUniqueIdWithSubject> = mutableListOf()
			val dayWithClasses = async { lessonDataSource.getAllWithSubjectByDate(date) }
			val dayInfo = async { webService.getDayInfo(date) }
			
			val classes = if (dayWithClasses.await().isEmpty()) {
				LessonParser()
					.parse(dayInfo = dayInfo.await(), localDate = date)
					.toSubjectEntitiesIndexed(subjectDataSource, studyDayDataSource)
			} else {
				val newMap = mutableMapOf<Int, SubjectEntity>()
				dayWithClasses.await().forEach {
					newMap[it.lesson.index] = it.subject
				}
				newMap
			}
			
			classes.forEach { subjectIndexed ->
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
	
	private suspend fun fetchTasksForSubject(
		subjectIndexed: Map.Entry<Int, SubjectEntity>,
		dayInfo: Elements,
		date: LocalDate,
	): List<TaskAndUniqueIdWithSubject> = withContext(ioDispatcher) {
		val lessonNewTasks: MutableList<TaskAndUniqueIdWithSubject> = mutableListOf()
		val netTaskVariants = TaskParser().parse(
			dayInfo = dayInfo,
			date = date,
			subject = subjectIndexed.value,
		)
		if (netTaskVariants.isNotEmpty()) {
			Log.d(
				TAG, "fetchSoonTasks: taskVariant found on date($date) for:\n" +
						"subject(${subjectIndexed.value.getName()}," +
						" id = ${subjectIndexed.value.subjectId})\n" +
						"taskVariant(size = ${netTaskVariants.size}): ${netTaskVariants[0]}" +
						(if (netTaskVariants.size > 1) netTaskVariants[1] else "") +
						(if (netTaskVariants.size > 2) "..." else "")
			)
			
			val localTasks = getByDateAndSubject(date, subjectIndexed.value.subjectId)
			if (localTasks.isNotEmpty()) {
				Log.d(
					TAG,
					"fetchSoonTasks: local tasks found on date($date) for:\n" +
							"subject(${subjectIndexed.value.getName()}," +
							" id = ${subjectIndexed.value.subjectId})\n" +
							"localTasks(size = ${localTasks.size}): ${localTasks[0]}" +
							(if (localTasks.size > 1) localTasks[1] else "") +
							(if (localTasks.size > 2) "..." else "")
				)
				var deletedCounter = 0
				localTasks.forEach { taskEntity ->
					if (taskEntity.isFetched) {
						deleteTask(taskEntity.taskId)
						deletedCounter++
					}
				}
				Log.d(
					TAG, "fetchSoonTasks: local tasks old fetched deleted" +
							"(count = $deletedCounter) on date($date) for:\n" +
							"subject(${subjectIndexed.value.getName()}," +
							" id = ${subjectIndexed.value.subjectId})\n"
				)
				if (deletedCounter == netTaskVariants.size) {
					taskDataSource.upsertAll(netTaskVariants.toTaskEntities())
					Log.d(
						TAG,
						"fetchSoonTasks: deleted enough fetched tasks, so " +
								"adding\n" +
								"taskVariant(size = ${netTaskVariants.size}): ${netTaskVariants[0]}" +
								(if (netTaskVariants.size > 1) netTaskVariants[1] else "") +
								(if (netTaskVariants.size > 2) "..." else "") + "\n" +
								"subject(${subjectIndexed.value.getName()}, " +
								"id = ${subjectIndexed.value.subjectId})\n"
					)
				} else {
					Log.d(
						TAG,
						"fetchSoonTasks: didn't delete enough fetched tasks, so " +
								"NOT adding\n" +
								"taskVariant(size = ${netTaskVariants.size}): ${netTaskVariants[0]}" +
								(if (netTaskVariants.size > 1) netTaskVariants[1] else "") +
								(if (netTaskVariants.size > 2) "..." else "") + "\n" +
								"subject(${subjectIndexed.value.getName()}, " +
								"id = ${subjectIndexed.value.subjectId})\n"
					)
				}
			} else {
				lessonNewTasks.addAll(netTaskVariants.toTasksAndUniqueIdWithSubject())
				taskDataSource.upsertAll(netTaskVariants.toTaskEntities())
				Log.d(
					TAG,
					"fetchSoonTasks($date, ${subjectIndexed.value.getName()}):" +
							" localTasks.isEmpty so just saving\n $netTaskVariants"
				)
			}
		} else {
			Log.d(
				TAG,
				"fetchSoonTasks: netTaskVariant not found on date($date) for:\n" +
						"subject(${subjectIndexed.value.getName()}," +
						" id = ${subjectIndexed.value.subjectId})"
			)
		}
		return@withContext lessonNewTasks
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
	
	override suspend fun getById(taskId: Long): TaskEntity? {
		return taskDataSource.getById(taskId)
	}
	
	override suspend fun getByDateAndSubject(date: LocalDate, subjectId: String): List<TaskEntity> {
		return taskDataSource.getByDateAndSubject(date = date, subjectId = subjectId)
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