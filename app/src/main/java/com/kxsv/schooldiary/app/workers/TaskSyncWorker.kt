package com.kxsv.schooldiary.app.workers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.data.local.features.task.TaskAndUniqueIdWithSubject
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.di.util.NotificationsConstants
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_TASKS_GROUP_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_TASKS_SUMMARY_ID
import com.kxsv.schooldiary.util.Utils.measurePerformanceInMS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val TAG = "TaskSyncWorker"

@HiltWorker
class TaskSyncWorker @AssistedInject constructor(
	@Assisted private val taskRepository: TaskRepository,
	@Assisted(FETCHED_TASKS_SUMMARY_ID) private val taskSummaryNotificationBuilder: Notification.Builder,
	@Assisted(FETCHED_TASKS_GROUP_ID) private val taskNotificationBuilder: Notification.Builder,
	@Assisted private val notificationManager: NotificationManager,
	@Assisted private val context: Context,
	@Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {
	
	override suspend fun doWork(): Result {
		try {
			val newTaskEntities = measurePerformanceInMS(
				logger = { time, _ ->
					Log.i(
						TAG, "doWork: performance is" +
								" ${(time / 10f).roundToInt() / 100f} S"
					)
				}
			) {
				taskRepository.fetchSoonTasks()
			}
			if (ActivityCompat
					.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
				PackageManager.PERMISSION_GRANTED
			) {
				Log.d(TAG, "DEBUG newTaskEntities: $newTaskEntities")
				if (newTaskEntities.isNotEmpty()) {
					notificationManager.notify(3, createSummaryNotification())
					newTaskEntities.forEach {
						notificationManager.notify(
							it.uniqueId, 4, createNotification(it)
						)
					}
				}
				
			} else {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				
				return Result.success()
			}
			return Result.success()
		} catch (e: Exception) {
			when (e) {
				is NetworkException -> {
					Log.e(TAG, "fetchSoonTasks: NetworkException on fetch", e)
					return Result.failure()
				}
				
				is IOException -> {
					Log.e(TAG, "fetchSoonTasks: exception on response parse", e)
					return Result.failure()
				}
				
				is TimeoutCancellationException -> {
					Log.e(TAG, "fetchSoonTasks: connection timed-out", e)
					return Result.retry()
					// TODO: show message that couldn't connect to site
				}
				
				else -> {
					Log.e(TAG, "fetchSoonTasks: exception", e)
					return Result.failure()
				}
			}
		}
	}
	
	private fun createSummaryNotification(): Notification {
		createNotificationChannel()
		return taskSummaryNotificationBuilder.build()
	}
	
	private fun createNotification(taskWithSubject: TaskAndUniqueIdWithSubject): Notification {
		createNotificationChannel()
		val title = taskWithSubject.taskEntity.title
		val text = title/*.take(27) + if (title.length > 27) "..." else ""*/ +
				" Due date: " + taskWithSubject.taskEntity.dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
		return taskNotificationBuilder.setContentText(text)
			.setContentTitle(taskWithSubject.subject.getName()).build()
	}
	
	private fun createNotificationChannel() {
		val taskChannel = NotificationChannel(
			NotificationsConstants.TASK_CHANNEL_ID,
			"Task Channel",
			NotificationManager.IMPORTANCE_DEFAULT
		)
		
		val notificationManager: NotificationManager? =
			ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)
		
		notificationManager?.createNotificationChannel(taskChannel)
	}
}