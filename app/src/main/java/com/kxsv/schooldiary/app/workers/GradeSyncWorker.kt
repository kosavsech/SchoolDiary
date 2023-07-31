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
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_GRADES_GROUP_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_GRADES_SUMMARY_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.GRADE_CHANNEL_ID
import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.remote.NetworkException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.TimeoutCancellationException
import java.io.IOException
import kotlin.math.roundToInt

private const val TAG = "GradeSyncWorker"

@HiltWorker
class GradeSyncWorker @AssistedInject constructor(
	@Assisted private val gradeRepository: GradeRepository,
	@Assisted(FETCHED_GRADES_SUMMARY_ID) private val gradeSummaryNotificationBuilder: Notification.Builder,
	@Assisted(FETCHED_GRADES_GROUP_ID) private val gradeNotificationBuilder: Notification.Builder,
	@Assisted private val notificationManager: NotificationManager,
	@Assisted private val context: Context,
	@Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {
	
	override suspend fun doWork(): Result {
		try {
			val newGradeEntities = Utils.measurePerformanceInMS(
				logger = { time, _ ->
					Log.i(
						TAG, "doWork: performance is" +
								" ${(time / 10f).roundToInt() / 100f} S"
					)
				}
			) {
				gradeRepository.fetchRecentGrades()
			}
			if (ActivityCompat
					.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
				PackageManager.PERMISSION_GRANTED
			) {
				Log.d(TAG, "DEBUG $newGradeEntities")
				if (newGradeEntities.isNotEmpty()) {
					notificationManager.notify(2, createSummaryNotification())
					newGradeEntities.forEach {
						notificationManager.notify(
							it.grade.gradeId, 1, createNotification(gradeEntity = it)
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
					Log.e(TAG, "fetchRecentGrades: exception on login", e)
					return Result.failure()
				}
				
				is IOException -> {
					Log.e(TAG, "fetchRecentGrades: exception on response parse", e)
					return Result.failure()
				}
				
				is TimeoutCancellationException -> {
					Log.e(TAG, "fetchRecentGrades: connection timed-out", e)
					return Result.retry()
					// TODO: show message that couldn't connect to site
				}
				
				else -> {
					Log.e(TAG, "fetchRecentGrades: exception", e)
					return Result.failure()
				}
			}
		}
	}
	
	private fun createSummaryNotification(): Notification {
		createNotificationChannel()
		return gradeSummaryNotificationBuilder.build()
	}
	
	private fun createNotification(gradeEntity: GradeWithSubject): Notification {
		createNotificationChannel()
		
		val text = Mark.getStringValueFrom(gradeEntity.grade.mark) + " " + gradeEntity.grade.date
		return gradeNotificationBuilder.setContentText(text)
			.setContentTitle(gradeEntity.subject.getName()).build()
	}
	
	private fun createNotificationChannel() {
		val gradeChannel = NotificationChannel(
			GRADE_CHANNEL_ID,
			"Grade Channel",
			NotificationManager.IMPORTANCE_DEFAULT
		)
		
		val notificationManager: NotificationManager? =
			ContextCompat.getSystemService(applicationContext, NotificationManager::class.java)
		
		notificationManager?.createNotificationChannel(gradeChannel)
	}
}