package com.kxsv.schooldiary.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kxsv.schooldiary.app.workers.GradeSyncWorker
import com.kxsv.schooldiary.app.workers.ScheduleSyncWorker
import com.kxsv.schooldiary.app.workers.SubjectsSyncWorker
import com.kxsv.schooldiary.app.workers.TaskSyncWorker
import com.kxsv.schooldiary.ui.main.navigation.NavGraph
import com.kxsv.schooldiary.ui.screens.login.SplashViewModel
import com.kxsv.schooldiary.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
	
	@Inject
	lateinit var splashViewModel: SplashViewModel
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()
		
		// TODO: add user setting to edit this interval
		val gradeSyncWorkRequest =
			PeriodicWorkRequestBuilder<GradeSyncWorker>(360, TimeUnit.MINUTES)
				.setConstraints(constraints)
				.build()
		
		// TODO: add user setting to edit this interval
		val taskSyncWorkRequest = PeriodicWorkRequestBuilder<TaskSyncWorker>(360, TimeUnit.MINUTES)
			.setConstraints(constraints)
			.build()
		
		// TODO: add user setting to edit this interval
		val scheduleSyncWorkRequest =
			PeriodicWorkRequestBuilder<ScheduleSyncWorker>(360, TimeUnit.MINUTES)
				.setConstraints(constraints)
				.build()
		
		// TODO: add user setting to edit this interval
		val subjectsSyncWorkRequest =
			PeriodicWorkRequestBuilder<SubjectsSyncWorker>(24, TimeUnit.HOURS)
				.setConstraints(constraints)
				.build()
		
		val workManager = WorkManager.getInstance(applicationContext)
		workManager.enqueueUniquePeriodicWork(
			"SubjectsSyncWorker",
			ExistingPeriodicWorkPolicy.UPDATE,
			subjectsSyncWorkRequest
		)
		
		workManager.enqueueUniquePeriodicWork(
			"RecentGradesSyncWorker",
			ExistingPeriodicWorkPolicy.UPDATE,
			gradeSyncWorkRequest
		)
		
		workManager.enqueueUniquePeriodicWork(
			"SoonTasksSyncWorker",
			ExistingPeriodicWorkPolicy.UPDATE,
			taskSyncWorkRequest
		)
		
		workManager.enqueueUniquePeriodicWork(
			"ScheduleSyncWorker",
			ExistingPeriodicWorkPolicy.UPDATE,
			scheduleSyncWorkRequest
		)
		
		setContent {
			AppTheme() {
				NavGraph(
					startRoute = splashViewModel.startDestination.value,
					activity = this
				)
			}
		}
	}
}