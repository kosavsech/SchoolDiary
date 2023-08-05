package com.kxsv.schooldiary.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kxsv.schooldiary.app.workers.GradeSyncWorker
import com.kxsv.schooldiary.app.workers.TaskSyncWorker
import com.kxsv.schooldiary.ui.main.navigation.NavGraph
import com.kxsv.schooldiary.ui.screens.login.SplashViewModel
import com.kxsv.schooldiary.ui.theme.SchoolDiaryTheme
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
		val gradeSyncWorkRequest = PeriodicWorkRequestBuilder<GradeSyncWorker>(15, TimeUnit.MINUTES)
			.setConstraints(constraints)
			.build()
		
		// TODO: add user setting to edit this interval
		val taskSyncWorkRequest = PeriodicWorkRequestBuilder<TaskSyncWorker>(15, TimeUnit.MINUTES)
			.setConstraints(constraints)
			.build()
		
		val workManager = WorkManager.getInstance(applicationContext)
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
		
		setContent {
			SchoolDiaryTheme() {
				NavGraph(
					startRoute = splashViewModel.startDestination.value,
					activity = this
				)
			}
		}
	}
}