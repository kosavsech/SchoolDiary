package com.kxsv.schooldiary.app.sync.workers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.app.sync.initializers.SyncConstraints
import com.kxsv.schooldiary.app.sync.initializers.syncForegroundInfo
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.di.util.NotificationsConstants
import com.kxsv.schooldiary.di.util.NotificationsConstants.NETWORK_CHANNEL_GROUP_ID
import com.kxsv.schooldiary.di.util.TaskNotification
import com.kxsv.schooldiary.di.util.TaskSummaryNotification
import com.kxsv.schooldiary.util.Utils.measurePerformanceInMS
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.TimeoutCancellationException
import org.jsoup.HttpStatusException
import org.jsoup.UnsupportedMimeTypeException
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val TAG = "TaskSyncWorker"

@HiltWorker
class TaskSyncWorker @AssistedInject constructor(
	@Assisted private val appContext: Context,
	@Assisted workerParams: WorkerParameters,
	private val taskRepository: TaskRepository,
	@TaskSummaryNotification private val taskSummaryNotificationBuilder: Notification.Builder,
	@TaskNotification private val taskNotificationBuilder: Notification.Builder,
	private val notificationManager: NotificationManager,
) : CoroutineWorker(appContext, workerParams) {
	
	companion object {
		fun startUpSyncWork() =
			PeriodicWorkRequestBuilder<DelegatingWorker>(360, TimeUnit.MINUTES)
				.setConstraints(SyncConstraints)
				.setInputData(TaskSyncWorker::class.delegatedData())
				.build()
	}
	
	override suspend fun getForegroundInfo(): ForegroundInfo =
		appContext.syncForegroundInfo()
	
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
					.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) ==
				PackageManager.PERMISSION_GRANTED
			) {
				if (newTaskEntities.isNotEmpty()) {
					Log.d(TAG, "DEBUG newTaskEntities: $newTaskEntities")
					notificationManager.notify(3, createSummaryNotification())
					newTaskEntities.forEach {
						notificationManager.notify(
							it.taskEntity.taskId, 4, createNotification(it)
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
		} catch (e: NetworkException) {
			Log.e(TAG, "fetchSoonTasks: NetworkException on fetch", e)
			return Result.failure()
		} catch (e: TimeoutCancellationException) {
			Log.e(TAG, "fetchSoonTasks: connection timed-out", e)
			// TODO: show message that couldn't connect to site
			return Result.retry()
		} catch (e: java.net.MalformedURLException) {
			return Result.failure()
		} catch (e: HttpStatusException) {
			Log.e(TAG, "fetchSoonTasks: exception on connect")
			return Result.failure()
		} catch (e: UnsupportedMimeTypeException) {
			return Result.failure()
		} catch (e: java.net.SocketTimeoutException) {
			return Result.failure()
		} catch (e: IOException) {
			Log.e(TAG, "fetchSoonTasks: exception on response parse", e)
			return Result.failure()
		} catch (e: Exception) {
			Log.e(TAG, "fetchSoonTasks: exception", e)
			return Result.failure()
		}
	}
	
	private fun createSummaryNotification(): Notification {
		createNotificationChannel()
		return taskSummaryNotificationBuilder.build()
	}
	
	private fun createNotification(taskWithSubject: TaskWithSubject): Notification {
		createNotificationChannel()
		val subjectName = taskWithSubject.subject.getName()
		val title = subjectName.take(10) + if (subjectName.length > 10) "... " else " " +
				taskWithSubject.taskEntity.dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
		val text = taskWithSubject.taskEntity.title
		
		return taskNotificationBuilder
			.setContentTitle(title)
			.setContentText(text)
			.build()
	}
	
	private fun createNotificationChannel() {
		notificationManager.createNotificationChannelGroup(
			NotificationChannelGroup(NETWORK_CHANNEL_GROUP_ID, "Background sync")
		)
		val taskChannel = NotificationChannel(
			NotificationsConstants.TASK_CHANNEL_ID,
			"Fetched Tasks",
			NotificationManager.IMPORTANCE_DEFAULT
		).apply {
			description =
				"Notifies about new grades which were fetched."
			group = NETWORK_CHANNEL_GROUP_ID
		}
		
		notificationManager.createNotificationChannel(taskChannel)
	}
}