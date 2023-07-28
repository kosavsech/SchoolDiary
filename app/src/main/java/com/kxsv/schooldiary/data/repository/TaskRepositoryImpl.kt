package com.kxsv.schooldiary.data.repository

import android.util.Log
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskDao
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.mapper.toLocalWithSubject
import com.kxsv.schooldiary.data.mapper.toTaskEntities
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.lesson.ScheduleParser
import com.kxsv.schooldiary.data.remote.task.TaskDto
import com.kxsv.schooldiary.data.remote.task.TaskParser
import com.kxsv.schooldiary.di.ApplicationScope
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.util.Utils.rangeToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
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
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
	@ApplicationScope private val scope: CoroutineScope,
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
	
	override suspend fun fetchSoonTasks(): Unit = withContext(ioDispatcher) {
		// todo change to NOW
		val startRange = LocalDate.of(2023, 2, 11)
		val period = (startRange..startRange.plusDays(7)).rangeToList()
		period.forEach { date ->
			if (date.dayOfWeek == DayOfWeek.SUNDAY) return@forEach
			async {
				Log.d(TAG, "fetchSoonTasks: started for date $date")
				val dayWithClasses = studyDayDataSource.getByDateWithSchedulesAndSubjects(date)
				var dayInfo: Elements? = null
				val classes = if (dayWithClasses != null && dayWithClasses.classes.isNotEmpty()) {
					dayWithClasses.classes.associateBy { it.lesson.index }
				} else {
					dayInfo = webService.getDayInfo(date)
					ScheduleParser().parse(dayInfo = dayInfo, localDate = date)
						.toLocalWithSubject(subjectDataSource, studyDayDataSource)
				}
				classes.forEach { classWithSubject ->
					if (dayInfo == null) dayInfo = webService.getDayInfo(date)
					val taskVariant = TaskParser().parse(
						dayInfo = dayInfo!!,
						date = date,
						subject = classWithSubject.value.subject,
					)
					if (taskVariant.isNotEmpty()) {
						Log.d(
							TAG,
							"fetchSoonTasks($date, ${classWithSubject.value.subject.getName()}):\n" +
									" taskVariant found ${taskVariant.size}: ${taskVariant[0]}" +
									(if (taskVariant.size > 1) taskVariant[1] else "") +
									if (taskVariant.size > 2) "..." else ""
						)
						val localTasks =
							getByDateAndSubject(date, classWithSubject.value.subject.subjectId)
						if (localTasks.isNotEmpty()) {
							Log.d(
								TAG,
								"fetchSoonTasks($date, ${classWithSubject.value.subject.getName()}):" +
										" localTasks.isNotEmpty"
							)
							var deletedCounter = 0
							localTasks.forEach { taskEntity ->
								if (taskEntity.isFetched) {
									deleteTask(taskEntity.taskId)
									deletedCounter++
								}
							}
							Log.d(
								TAG,
								"fetchSoonTasks($date, ${classWithSubject.value.subject.getName()}):\n" +
										" deleted old fetched: $deletedCounter"
							)
							if (deletedCounter == taskVariant.size) {
								taskDataSource.upsertAll(taskVariant.toTaskEntities())
								Log.i(
									TAG,
									"fetchSoonTasks($date, ${classWithSubject.value.subject.getName()}):" +
											" saving:\n $taskVariant"
								)
							} else {
								Log.i(
									TAG,
									"fetchSoonTasks($date, ${classWithSubject.value.subject.getName()}):" +
											" NOT saving:\n $taskVariant"
								)
							}
						} else {
							taskDataSource.upsertAll(taskVariant.toTaskEntities())
							Log.d(
								TAG,
								"fetchSoonTasks($date, ${classWithSubject.value.subject.getName()}):" +
										" localTasks.isEmpty so just saving\n $taskVariant"
							)
						}
					} else {
						Log.d(TAG, "fetchSoonTasks: taskVariant not found")
					}
					
				}
			}
		}
	}
	
	
	override suspend fun fetchTaskByDate(date: LocalDate, subject: SubjectEntity): List<TaskDto> {
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
	
	override suspend fun getByDateAndSubject(date: LocalDate, subjectId: Long): List<TaskEntity> {
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