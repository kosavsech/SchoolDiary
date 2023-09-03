package com.kxsv.schooldiary.app.sync.initializers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import com.kxsv.schooldiary.R


private const val SYNC_NOTIFICATION_ID = 99
private const val SYNC_NOTIFICATION_CHANNEL_ID = "SyncNotificationChannel"


// All sync work needs an internet connectionS
val SyncConstraints
	get() = Constraints.Builder()
		.setRequiredNetworkType(NetworkType.CONNECTED)
		.build()

/**
 * Foreground information for sync on lower API levels when sync workers are being
 * run with a foreground service
 */
fun Context.syncForegroundInfo() = ForegroundInfo(
	SYNC_NOTIFICATION_ID,
	syncWorkNotification(),
)

/**
 * Notification displayed on lower API(< 31) levels when sync workers are being
 * run with a foreground service
 */
private fun Context.syncWorkNotification(): Notification {
	val channel = NotificationChannel(
		SYNC_NOTIFICATION_CHANNEL_ID,
		"Sync",
		NotificationManager.IMPORTANCE_LOW,
	).apply {
		description = "Background tasks for School diary"
	}
	
	val notificationManager: NotificationManager? =
		getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
	
	notificationManager?.createNotificationChannel(channel)
	
	return NotificationCompat.Builder(this, SYNC_NOTIFICATION_CHANNEL_ID)
		.setSmallIcon(R.mipmap.ic_launcher)
		.setContentTitle("Syncing...")
		.setDefaults(NotificationCompat.DEFAULT_ALL)
		.setPriority(NotificationCompat.PRIORITY_LOW)
		.build()
}
