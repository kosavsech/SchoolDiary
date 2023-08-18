package com.kxsv.schooldiary.app.sync.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.app.sync.initializers.SyncConstraints
import com.kxsv.schooldiary.app.sync.initializers.syncForegroundInfo
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.util.DataIdGenUtils
import com.kxsv.schooldiary.di.util.AppDispatchers.IO
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val TAG = "SubjectsSyncWorker"

@HiltWorker
class SubjectsSyncWorker @AssistedInject constructor(
	@Assisted private val appContext: Context,
	@Assisted workerParams: WorkerParameters,
	private val subjectRepository: SubjectRepository,
	@Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {
	
	companion object {
		fun startUpSyncWork() =
			PeriodicWorkRequestBuilder<DelegatingWorker>(24, TimeUnit.HOURS)
				.setConstraints(SyncConstraints)
				.setInputData(SubjectsSyncWorker::class.delegatedData())
				.build()
	}
	
	override suspend fun getForegroundInfo(): ForegroundInfo =
		appContext.syncForegroundInfo()
	
	override suspend fun doWork(): Result {
		try {
			val subjectNames = Utils.measurePerformanceInMS(
				logger = { time, _ ->
					Log.i(
						TAG, "doWork: performance is" +
								" ${(time / 10f).roundToInt() / 100f} S"
					)
				}
			) {
				subjectRepository.fetchSubjectNames()
			}
			val newSubjectsFound = Utils.measurePerformanceInMS(
				logger = { time, _ -> Log.i(TAG, "updateDatabase: performance is $time MS") }
			) {
				val fetchedSubjects = subjectNames.map { SubjectEntity(fullName = it) }
				updateDatabase(fetchedSubjects)
			}
			return Result.success()
		} catch (e: Exception) {
			when (e) {
				is NetworkException -> {
					Log.e(TAG, "fetchSubjectNames: you're not logged in", e)
					return Result.failure()
				}
				
				is IOException -> {
					Log.e(TAG, "fetchSubjectNames: exception on response parse", e)
					return Result.failure()
				}
				
				is TimeoutCancellationException -> {
					Log.e(TAG, "fetchSubjectNames: connection timed-out", e)
					return Result.retry()
					// TODO: show message that couldn't connect to site
				}
				
				else -> {
					Log.e(TAG, "fetchSubjectNames: exception", e)
					return Result.failure()
				}
			}
		}
	}
	
	private suspend fun updateDatabase(fetchedSubjects: List<SubjectEntity>): List<SubjectEntity> {
		return withContext(ioDispatcher) {
			val newSubjectsFound: MutableList<SubjectEntity> = mutableListOf()
			fetchedSubjects.forEach { subject ->
				val subjectId = DataIdGenUtils.generateSubjectId(subject.fullName)
				val existedSubject = Utils.measurePerformanceInMS(
					{ time, result ->
						Log.d(
							TAG, "getSubjectByName(${subject.fullName}): $time ms\n found = $result"
						)
					}
				) {
					subjectRepository.getSubject(subjectId)
				}
				if (existedSubject == null) {
					val newSubject = subject.copy(subjectId = subjectId)
					Log.i(TAG, "updateDatabase: FOUND NEW SUBJECT:\n${newSubject.fullName}")
					newSubjectsFound.add(newSubject)
					subjectRepository.createSubject(newSubject, null)
				}
			}
			newSubjectsFound
		}
	}
}
