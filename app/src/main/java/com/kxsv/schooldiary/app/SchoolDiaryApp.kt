package com.kxsv.schooldiary.app

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.app.workers.GradeSyncWorker
import com.kxsv.schooldiary.app.workers.TaskSyncWorker
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.TaskRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

private const val TAG = "SchoolDiaryApp"

@HiltAndroidApp
class SchoolDiaryApp : Application(), Configuration.Provider {
	
	@Inject
	lateinit var customSyncWorkerFactory: CustomSyncWorkerFactory
	
	override fun getWorkManagerConfiguration(): Configuration =
		Configuration.Builder()
			.setMinimumLoggingLevel(Log.DEBUG)
			.setWorkerFactory(customSyncWorkerFactory)
			.build()
	
}

class CustomSyncWorkerFactory @Inject constructor(
	private val gradeRepository: GradeRepository,
	private val taskRepository: TaskRepository,
) : WorkerFactory() {
	override fun createWorker(
		appContext: Context,
		workerClassName: String,
		workerParameters: WorkerParameters,
	): ListenableWorker? {
		Log.i(TAG, "createWorker: launched")
		
		return when (workerClassName) {
			GradeSyncWorker::class.java.name -> {
				Log.i(TAG, "createWorker: launched GradeSyncWorker")
				GradeSyncWorker(gradeRepository, appContext, workerParameters)
			}
			
			TaskSyncWorker::class.java.name -> {
				Log.i(TAG, "createWorker: launched TaskSyncWorker")
				TaskSyncWorker(taskRepository, appContext, workerParameters)
			}
			
			else -> {
				Log.e(TAG, "createWorker: unknown workerClassName = $workerClassName")
				null
			}
		}
	}
	
}