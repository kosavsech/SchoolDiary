package com.kxsv.schooldiary.app.sync.workers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.app.MainActivity
import com.kxsv.schooldiary.app.sync.initializers.SyncConstraints
import com.kxsv.schooldiary.app.sync.initializers.syncForegroundInfo
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.di.util.NotificationsConstants
import com.kxsv.schooldiary.di.util.ScheduleNotification
import com.kxsv.schooldiary.di.util.ScheduleSummaryNotification
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
import com.kxsv.schooldiary.ui.screens.grade_list.MY_URI
import com.kxsv.schooldiary.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.TimeoutCancellationException
import org.jsoup.HttpStatusException
import org.jsoup.UnsupportedMimeTypeException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val TAG = "ScheduleSyncWorker"

@HiltWorker
class ScheduleSyncWorker @AssistedInject constructor(
	@Assisted private val appContext: Context,
	@Assisted workerParams: WorkerParameters,
	private val lessonRepository: LessonRepository,
	@ScheduleSummaryNotification private val scheduleSummaryNotificationBuilder: Notification.Builder,
	@ScheduleNotification private val scheduleNotificationBuilder: Notification.Builder,
	private val notificationManager: NotificationManager,
) : CoroutineWorker(appContext, workerParams) {
	
	companion object {
		fun startUpSyncWork() =
			PeriodicWorkRequestBuilder<DelegatingWorker>(360, TimeUnit.MINUTES)
				.setConstraints(SyncConstraints)
				.setInputData(ScheduleSyncWorker::class.delegatedData())
				.build()
	}
	
	override suspend fun getForegroundInfo(): ForegroundInfo =
		appContext.syncForegroundInfo()
	
	override suspend fun doWork(): Result {
		try {
			val newSchedules = Utils.measurePerformanceInMS(
				logger = { time, _ ->
					Log.i(
						TAG, "doWork: performance is" +
								" ${(time / 10f).roundToInt() / 100f} S"
					)
				}
			) {
				lessonRepository.fetchSoonSchedule()
			}
			if (ActivityCompat.checkSelfPermission(
					appContext,
					Manifest.permission.POST_NOTIFICATIONS
				)
				== PackageManager.PERMISSION_GRANTED
			) {
				Log.d(TAG, "DEBUG newSchedules: $newSchedules")
				if (newSchedules.isNotEmpty()) {
					notificationManager.notify(5, createSummaryNotification())
					newSchedules.forEach {
						val uniqueId =
							it.key.month.value.toString() + "_" + it.key.dayOfMonth.toString()
						Log.d(TAG, "doWork: created unique id is $uniqueId")
						notificationManager.notify(
							uniqueId,
							6,
							createNotification(schedule = it)
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
			Log.e(TAG, "fetchSoonSchedule: NetworkException on fetch", e)
			return Result.failure()
		} catch (e: TimeoutCancellationException) {
			Log.e(TAG, "fetchSoonSchedule: connection timed-out", e)
			// TODO: show message that couldn't connect to site
			return Result.retry()
		} catch (e: java.net.MalformedURLException) {
			return Result.failure()
		} catch (e: HttpStatusException) {
			Log.e(TAG, "fetchSoonSchedule: exception on connect")
			return Result.failure()
		} catch (e: UnsupportedMimeTypeException) {
			return Result.failure()
		} catch (e: java.net.SocketTimeoutException) {
			return Result.failure()
		} catch (e: IOException) {
			Log.e(TAG, "fetchSoonSchedule: exception on response parse", e)
			return Result.failure()
		} catch (e: Exception) {
			Log.e(TAG, "fetchSoonSchedule: exception", e)
			return Result.failure()
		}
	}
	
	
	private fun createSummaryNotification(): Notification {
		createNotificationChannel()
		return scheduleSummaryNotificationBuilder.build()
	}
	
	private fun createNotification(schedule: Map.Entry<LocalDate, Utils.ScheduleCompareResult>): Notification {
		createNotificationChannel()
		val title = if (schedule.value.isNew) {
			"New schedule"
		} else {
			"Different schedule"
		}
		val text = if (schedule.value.isNew) {
			"Fetched on date: " +
					schedule.key.format(DateTimeFormatter.ISO_LOCAL_DATE)
		} else {
			"Fetched on date: " +
					schedule.key.format(DateTimeFormatter.ISO_LOCAL_DATE)
		}
		
		val validDayScheduleScreenRoute = if (schedule.value.isNew) {
			DayScheduleScreenDestination(
				datestamp = Utils.localDateToTimestamp(schedule.key),
				showComparison = false
			).route
		} else {
			DayScheduleScreenDestination(
				datestamp = Utils.localDateToTimestamp(schedule.key),
				showComparison = true
			).route
		}
		
		val uriForDayScheduleScreen = "$MY_URI/$validDayScheduleScreenRoute".toUri()
		val clickIntent = Intent(
			Intent.ACTION_VIEW,
			uriForDayScheduleScreen,
			appContext,
			MainActivity::class.java
		)
		val clickPendingIntent: PendingIntent = TaskStackBuilder.create(appContext).run {
			addNextIntentWithParentStack(clickIntent)
			getPendingIntent(5, PendingIntent.FLAG_IMMUTABLE)
		}
		
		return scheduleNotificationBuilder
			.setContentTitle(title)
			.setContentText(text)
			.setContentIntent(clickPendingIntent)
			.build()
		
	}
	
	private fun createNotificationChannel() {
		notificationManager.createNotificationChannelGroup(
			NotificationChannelGroup(
				NotificationsConstants.NETWORK_CHANNEL_GROUP_ID,
				"Background sync"
			)
		)
		val taskChannel = NotificationChannel(
			NotificationsConstants.SCHEDULE_CHANNEL_ID,
			"Fetched Schedule",
			NotificationManager.IMPORTANCE_DEFAULT
		).apply {
			description =
				"Notifies if new schedule were fetched or fetched schedule is differs from local one."
			group = NotificationsConstants.NETWORK_CHANNEL_GROUP_ID
		}
		
		notificationManager.createNotificationChannel(taskChannel)
	}
}