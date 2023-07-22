package com.kxsv.schooldiary.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.app.workers.GradeSyncWorker
import com.kxsv.schooldiary.data.repository.GradeRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class SchoolDiaryApp : Application(), Configuration.Provider {
	
	@Inject
	lateinit var gradeSyncWorkerFactory: GradeSyncWorkerFactory
	
	override fun getWorkManagerConfiguration(): Configuration =
		Configuration.Builder()
			.setMinimumLoggingLevel(Log.DEBUG)
			.setWorkerFactory(gradeSyncWorkerFactory)
			.build()
	
}

class GradeSyncWorkerFactory @Inject constructor(
	private val gradeRepository: GradeRepository,
) : WorkerFactory() {
	override fun createWorker(
		appContext: Context,
		workerClassName: String,
		workerParameters: WorkerParameters,
	): ListenableWorker = GradeSyncWorker(gradeRepository, appContext, workerParameters)
	
}