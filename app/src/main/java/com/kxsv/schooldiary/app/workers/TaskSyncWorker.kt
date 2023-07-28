package com.kxsv.schooldiary.app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.data.repository.TaskRepository
import com.kxsv.schooldiary.util.Utils.measurePerformanceInMS
import com.kxsv.schooldiary.util.remote.NetworkException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.math.roundToInt

private const val TAG = "TaskSyncWorker"

@HiltWorker
class TaskSyncWorker @AssistedInject constructor(
	@Assisted private val taskRepository: TaskRepository,
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {
	
	override suspend fun doWork(): Result {
		try {
			withTimeout(15000L) {
				measurePerformanceInMS(
					logger = { time, _ ->
						Log.i(
							TAG, "doWork: performance is" +
									" ${(time / 10f).roundToInt() / 100f} S"
						)
					}
				) {
					taskRepository.fetchSoonTasks()
				}
			}
			return Result.success()
		} catch (e: Exception) {
			when (e) {
				is NetworkException -> {
					Log.e(TAG, "fetchSoonTasks: exception on login", e)
					return Result.failure()
				}
				
				is IOException -> {
					Log.e(TAG, "fetchSoonTasks: exception on response parse", e)
					return Result.failure()
				}
				
				is TimeoutCancellationException -> {
					Log.e(TAG, "fetchSoonTasks: connection timed-out", e)
					return Result.retry()
					// TODO: show message that couldn't connect to site
				}
				
				else -> {
					Log.e(TAG, "fetchSoonTasks: exception", e)
					return Result.failure()
				}
			}
		}
	}
}