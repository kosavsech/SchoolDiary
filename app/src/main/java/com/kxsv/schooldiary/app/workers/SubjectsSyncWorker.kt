package com.kxsv.schooldiary.app.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.util.remote.NetworkException
import com.kxsv.schooldiary.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.roundToInt

private const val TAG = "SubjectsSyncWorker"

@HiltWorker
class SubjectsSyncWorker @AssistedInject constructor(
	@Assisted private val subjectRepository: SubjectRepository,
	@Assisted private val context: Context,
	@Assisted private val ioDispatcher: CoroutineDispatcher,
	@Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {
	
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
			newSubjectsFound.forEach {
				subjectRepository.createSubject(it, emptySet())
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
			for (subject in fetchedSubjects) {
				val isGradeExisted = Utils.measurePerformanceInMS(
					{ time, result ->
						Log.d(
							TAG, "getSubjectByName(${subject.fullName}): $time ms\n found = $result"
						)
					}
				) {
					subjectRepository.getSubjectByName(subject.fullName) != null
				}
				if (!isGradeExisted) {
					newSubjectsFound.add(subject)
					Log.i(TAG, "updateDatabase: FOUND NEW SUBJECT:\n${subject.fullName}")
				}
			}
			newSubjectsFound
		}
	}
}
