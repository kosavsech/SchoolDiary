package com.kxsv.schooldiary.app

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.app.workers.GradeSyncWorker
import com.kxsv.schooldiary.app.workers.ScheduleSyncWorker
import com.kxsv.schooldiary.app.workers.SubjectsSyncWorker
import com.kxsv.schooldiary.app.workers.TaskSyncWorker
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.SubjectTeacherRepository
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.data.repository.TeacherRepository
import com.kxsv.schooldiary.di.util.GradeNotification
import com.kxsv.schooldiary.di.util.GradeSummaryNotification
import com.kxsv.schooldiary.di.util.IoDispatcher
import com.kxsv.schooldiary.di.util.ScheduleNotification
import com.kxsv.schooldiary.di.util.ScheduleSummaryNotification
import com.kxsv.schooldiary.di.util.TaskNotification
import com.kxsv.schooldiary.di.util.TaskSummaryNotification
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

private const val TAG = "SchoolDiaryApp"

@HiltAndroidApp
class SchoolDiaryApp : Application(), Configuration.Provider {
	
	@Inject
	lateinit var customWorkerFactory: CustomWorkerFactory
	
	override fun getWorkManagerConfiguration(): Configuration =
		Configuration.Builder()
			.setMinimumLoggingLevel(Log.DEBUG)
			.setWorkerFactory(customWorkerFactory)
			.build()
	
}

class CustomWorkerFactory @Inject constructor(
	private val subjectTeacherRepository: SubjectTeacherRepository,
	private val gradeRepository: GradeRepository,
	private val subjectRepository: SubjectRepository,
	private val teacherRepository: TeacherRepository,
	private val taskRepository: TaskRepository,
	private val lessonRepository: LessonRepository,
	@GradeNotification private val gradeNotificationBuilder: Notification.Builder,
	@GradeSummaryNotification private val gradeSummaryNotificationBuilder: Notification.Builder,
	@TaskNotification private val taskNotificationBuilder: Notification.Builder,
	@TaskSummaryNotification private val taskSummaryNotificationBuilder: Notification.Builder,
	@ScheduleNotification private val scheduleNotificationBuilder: Notification.Builder,
	@ScheduleSummaryNotification private val scheduleSummaryNotificationBuilder: Notification.Builder,
	private val notificationManager: NotificationManager,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : WorkerFactory() {
	override fun createWorker(
		appContext: Context,
		workerClassName: String,
		workerParameters: WorkerParameters,
	): ListenableWorker? {
		Log.i(TAG, "createWorker: launched")
		
		return when (workerClassName) {
			GradeSyncWorker::class.java.name -> {
				Log.i(TAG, "createWorker: launched GradeSyncWorker")
				GradeSyncWorker(
					subjectTeacherRepository = subjectTeacherRepository,
					subjectRepository = subjectRepository,
					gradeRepository = gradeRepository,
					teacherRepository = teacherRepository,
					gradeSummaryNotificationBuilder = gradeSummaryNotificationBuilder,
					gradeNotificationBuilder = gradeNotificationBuilder,
					notificationManager = notificationManager,
					ioDispatcher = ioDispatcher,
					context = appContext,
					params = workerParameters
				)
			}
			
			TaskSyncWorker::class.java.name -> {
				Log.i(TAG, "createWorker: launched TaskSyncWorker")
				TaskSyncWorker(
					taskRepository = taskRepository,
					taskNotificationBuilder = taskNotificationBuilder,
					taskSummaryNotificationBuilder = taskSummaryNotificationBuilder,
					notificationManager = notificationManager,
					context = appContext,
					params = workerParameters
				)
			}
			
			ScheduleSyncWorker::class.java.name -> {
				Log.i(TAG, "createWorker: launched ScheduleSyncWorker")
				ScheduleSyncWorker(
					lessonRepository = lessonRepository,
					scheduleNotificationBuilder = scheduleNotificationBuilder,
					scheduleSummaryNotificationBuilder = scheduleSummaryNotificationBuilder,
					notificationManager = notificationManager,
					context = appContext,
					params = workerParameters
				)
			}
			
			SubjectsSyncWorker::class.java.name -> {
				Log.i(TAG, "createWorker: launched SubjectsSyncWorker")
				SubjectsSyncWorker(
					subjectRepository = subjectRepository,
					ioDispatcher = ioDispatcher,
					context = appContext,
					params = workerParameters
				)
			}
			
			else -> {
				Log.e(TAG, "createWorker: unknown workerClassName = $workerClassName")
				null
			}
		}
	}
	
}