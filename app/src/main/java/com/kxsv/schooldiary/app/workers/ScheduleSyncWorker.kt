package com.kxsv.schooldiary.app.workers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.app.MainActivity
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.di.util.NotificationsConstants
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_SCHEDULE_GROUP_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_SCHEDULE_SUMMARY_ID
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
import com.kxsv.schooldiary.ui.screens.grade_list.MY_URI
import com.kxsv.schooldiary.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private const val TAG = "ScheduleSyncWorker"

@HiltWorker
class ScheduleSyncWorker @AssistedInject constructor(
	@Assisted private val lessonRepository: LessonRepository,
	@Assisted(FETCHED_SCHEDULE_SUMMARY_ID) private val scheduleSummaryNotificationBuilder: Notification.Builder,
	@Assisted(FETCHED_SCHEDULE_GROUP_ID) private val scheduleNotificationBuilder: Notification.Builder,
	@Assisted private val notificationManager: NotificationManager,
	@Assisted private val context: Context,
	@Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {
	
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
			if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
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
		} catch (e: Exception) {
			when (e) {
				is NetworkException -> {
					Log.e(TAG, "fetchSchedule: NetworkException on fetch", e)
					return Result.failure()
				}
				
				is IOException -> {
					Log.e(TAG, "fetchSchedule: exception on response parse", e)
					return Result.failure()
				}
				
				is TimeoutCancellationException -> {
					Log.e(TAG, "fetchSchedule: connection timed-out", e)
					return Result.retry()
					// TODO: show message that couldn't connect to site
				}
				
				else -> {
					Log.e(TAG, "fetchSchedule: exception", e)
					return Result.failure()
				}
			}
		}
	}
	
	
	private fun createSummaryNotification(): Notification {
		createNotificationChannel()
		return scheduleSummaryNotificationBuilder.build()
	}
	
	private fun createNotification(schedule: Map.Entry<LocalDate, Utils.ScheduleCompareResult>): Notification {
		createNotificationChannel()
		val title = "Check it out!"
		val text = if (schedule.value.isNew) {
			"The new schedule fetched on date: " +
					schedule.key.format(DateTimeFormatter.ISO_LOCAL_DATE)
		} else {
			"The schedule differs from the saved one on date: " +
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
			context,
			MainActivity::class.java
		)
		val clickPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
			addNextIntentWithParentStack(clickIntent)
			getPendingIntent(5, PendingIntent.FLAG_IMMUTABLE)
		}
		
		return scheduleNotificationBuilder
			.setContentText(text)
			.setContentTitle(title)
			.setContentIntent(clickPendingIntent)
			.build()
		
	}
	
	private fun createNotificationChannel() {
		val taskChannel = NotificationChannel(
			NotificationsConstants.SCHEDULE_CHANNEL_ID,
			"Schedule Channel",
			NotificationManager.IMPORTANCE_DEFAULT
		)
		
		val notificationManager: NotificationManager? =
			ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)
		
		notificationManager?.createNotificationChannel(taskChannel)
	}
}