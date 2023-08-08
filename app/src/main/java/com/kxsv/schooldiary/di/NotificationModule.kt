package com.kxsv.schooldiary.di

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.di.util.GradeNotification
import com.kxsv.schooldiary.di.util.GradeSummaryNotification
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_GRADES_GROUP_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_SCHEDULE_GROUP_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.FETCHED_TASKS_GROUP_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.GRADE_CHANNEL_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.SCHEDULE_CHANNEL_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.TASK_CHANNEL_ID
import com.kxsv.schooldiary.di.util.ScheduleNotification
import com.kxsv.schooldiary.di.util.ScheduleSummaryNotification
import com.kxsv.schooldiary.di.util.TaskNotification
import com.kxsv.schooldiary.di.util.TaskSummaryNotification
import com.kxsv.schooldiary.ui.main.MainActivity
import com.kxsv.schooldiary.ui.screens.destinations.GradesScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TasksScreenDestination
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
	@GradeNotification
	fun provideGradeNotificationBuilder(
		@ApplicationContext context: Context,
	): Notification.Builder {
		
		val clickIntent = Intent(
			Intent.ACTION_VIEW,
			GradesScreenDestination.deepLinks.first().uriPattern!!.toUri(),
			context,
			MainActivity::class.java
		)
		val clickPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
			addNextIntentWithParentStack(clickIntent)
			getPendingIntent(1, PendingIntent.FLAG_IMMUTABLE)
		}
		
		return Notification.Builder(context, GRADE_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setVisibility(Notification.VISIBILITY_PRIVATE)
			.setAutoCancel(true)
			.setContentIntent(clickPendingIntent)
			.setPublicVersion(
				Notification.Builder(context, GRADE_CHANNEL_ID)
					.setContentTitle("Hidden")
					.setContentText("Unlock to see the message.")
					.build()
			)
			.setGroup(FETCHED_GRADES_GROUP_ID)
	}
	
	@Singleton
	@Provides
	@GradeSummaryNotification
	fun provideGradeSummaryNotificationBuilder(
		@ApplicationContext context: Context,
	): Notification.Builder {
		val clickIntent = Intent(
			Intent.ACTION_VIEW,
			GradesScreenDestination.deepLinks.first().uriPattern!!.toUri(),
			context,
			MainActivity::class.java
		)
		val clickPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
			addNextIntentWithParentStack(clickIntent)
			getPendingIntent(2, PendingIntent.FLAG_IMMUTABLE)
		}
		
		return Notification.Builder(context, GRADE_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setVisibility(Notification.VISIBILITY_PRIVATE)
			.setAutoCancel(true)
			.setContentIntent(clickPendingIntent)
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
			.setGroup(FETCHED_GRADES_GROUP_ID)
			.setGroupSummary(true)
	}
	
	@Singleton
	@Provides
	@TaskNotification
	fun provideTaskNotificationBuilder(
		@ApplicationContext context: Context,
	): Notification.Builder {
		
		val clickIntent = Intent(
			Intent.ACTION_VIEW,
			TasksScreenDestination.deepLinks.first().uriPattern!!.toUri(),
			context,
			MainActivity::class.java
		)
		val clickPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
			addNextIntentWithParentStack(clickIntent)
			getPendingIntent(3, PendingIntent.FLAG_IMMUTABLE)
		}
		
		return Notification.Builder(context, TASK_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setVisibility(Notification.VISIBILITY_PUBLIC)
			.setAutoCancel(true)
			.setContentIntent(clickPendingIntent)
			.setGroup(FETCHED_TASKS_GROUP_ID)
	}
	
	@Singleton
	@Provides
	@TaskSummaryNotification
	fun provideTaskSummaryNotificationBuilder(
		@ApplicationContext context: Context,
	): Notification.Builder {
		val clickIntent = Intent(
			Intent.ACTION_VIEW,
			TasksScreenDestination.deepLinks.first().uriPattern!!.toUri(),
			context,
			MainActivity::class.java
		)
		val clickPendingIntent: PendingIntent = TaskStackBuilder.create(context).run {
			addNextIntentWithParentStack(clickIntent)
			getPendingIntent(4, PendingIntent.FLAG_IMMUTABLE)
		}
		
		return Notification.Builder(context, TASK_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setVisibility(Notification.VISIBILITY_PUBLIC)
			.setAutoCancel(true)
			.setContentIntent(clickPendingIntent)
			.setStyle(
				Notification.InboxStyle()
					.setSummaryText("New tasks were fetched")
			)
			.setGroup(FETCHED_TASKS_GROUP_ID)
			.setGroupSummary(true)
	}
	
	@Singleton
	@Provides
	@ScheduleNotification
	fun provideScheduleNotificationBuilder(
		@ApplicationContext context: Context,
	): Notification.Builder {
		return Notification.Builder(context, SCHEDULE_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setVisibility(Notification.VISIBILITY_PUBLIC)
			.setAutoCancel(true)
			.setGroup(FETCHED_SCHEDULE_GROUP_ID)
	}
	
	@Singleton
	@Provides
	@ScheduleSummaryNotification
	fun provideScheduleSummaryNotificationBuilder(
		@ApplicationContext context: Context,
	): Notification.Builder {
		return Notification.Builder(context, SCHEDULE_CHANNEL_ID)
			.setSmallIcon(R.mipmap.ic_launcher)
			.setVisibility(Notification.VISIBILITY_PUBLIC)
			.setAutoCancel(false)
			.setStyle(
				Notification.InboxStyle()
					.setSummaryText("New schedules were fetched")
			)
			.setGroup(FETCHED_SCHEDULE_GROUP_ID)
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