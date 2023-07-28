package com.kxsv.schooldiary.app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.remote.NetworkException
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.io.IOException
import kotlin.math.roundToInt

private const val TAG = "GradeSyncWorker"

@HiltWorker
class GradeSyncWorker @AssistedInject constructor(
	@Assisted private val gradeRepository: GradeRepository,
	@Assisted context: Context,
	@Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {
	
	override suspend fun doWork(): Result {
		try {
			withTimeout(15000L) {
				Utils.measurePerformanceInMS(
					logger = { time, _ ->
						Log.i(
							TAG, "doWork: performance is" +
									" ${(time / 10f).roundToInt() / 100f} S"
						)
					}
				) {
					gradeRepository.fetchRecentGrades()
				}
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
}