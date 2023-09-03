package com.kxsv.schooldiary.app.sync.initializers

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.kxsv.schooldiary.app.sync.workers.SubjectsSyncWorker

object SubjectsSync {
	// This method is initializes sync, the process that keeps the app's data current.
	// It is called from the app module's Application.onCreate() and should be only done once.
	fun initialize(context: Context) {
		WorkManager.getInstance(context).apply {
			// Run sync on app startup and ensure only one sync worker runs at any time
			enqueueUniquePeriodicWork(
				SubjectsSyncWorkName,
				SubjectsSyncWorkPolicy,
				SubjectsSyncWorker.startUpSyncWork(),
			)
		}
	}
}

// This name should not be changed otherwise the app may have concurrent sync requests running
internal const val SubjectsSyncWorkName = "SubjectsSyncWorker"

internal val SubjectsSyncWorkPolicy = ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE
