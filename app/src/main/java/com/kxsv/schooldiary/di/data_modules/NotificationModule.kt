package com.kxsv.schooldiary.di.data_modules

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.di.util.NotificationBuilder
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_GRADE_GROUP_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.GRADE_CHANNEL_ID
import com.kxsv.schooldiary.di.util.SummaryNotificationBuilder
import com.kxsv.schooldiary.ui.main.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
	
	@Singleton
	@Provides
	@NotificationBuilder
	fun provideGradeNotificationBuilder(
		@ApplicationContext context: Context,
	): Notification.Builder {
		val mainActivityPendingIntent = PendingIntent.getActivity(
			context,
			0,
			Intent(context, MainActivity::class.java),
			PendingIntent.FLAG_IMMUTABLE
		)
		return Notification.Builder(context, GRADE_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setVisibility(Notification.VISIBILITY_PRIVATE)
			.setAutoCancel(true)
			.setContentIntent(mainActivityPendingIntent)
			.setPublicVersion(
				Notification.Builder(context, GRADE_CHANNEL_ID)
					.setContentTitle("Hidden")
					.setContentText("Unlock to see the message.")
					.build()
			)
			.setGroup(FETCHED_GRADE_GROUP_ID)
	}
	
	@Singleton
	@Provides
	@SummaryNotificationBuilder
	fun provideGradeSummaryNotificationBuilder(
		@ApplicationContext context: Context,
	): Notification.Builder {
		return Notification.Builder(context, GRADE_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setVisibility(Notification.VISIBILITY_PRIVATE)
			.setAutoCancel(true)
			.setStyle(
				Notification.InboxStyle()
					.setSummaryText("New grades were fetched")
			)
			.setPublicVersion(
				Notification.Builder(context, GRADE_CHANNEL_ID)
					.setContentTitle("Hidden")
					.setContentText("Unlock to see the message.")
					.build()
			)
			.setGroup(FETCHED_GRADE_GROUP_ID)
			.setGroupSummary(true)
	}
	
	@Singleton
	@Provides
	fun provideNotificationManager(
		@ApplicationContext context: Context,
	): NotificationManager {
		return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
	}
	
}