package com.kxsv.schooldiary.app.sync.workers

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.kxsv.schooldiary.app.sync.initializers.SyncConstraints
import com.kxsv.schooldiary.app.sync.initializers.syncForegroundInfo
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity.Companion.shortName
import com.kxsv.schooldiary.data.mapper.toGradeWithSubject
import com.kxsv.schooldiary.data.remote.dtos.TeacherDto
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.SubjectTeacherRepository
import com.kxsv.schooldiary.data.repository.TeacherRepository
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateTeacherId
import com.kxsv.schooldiary.data.util.Mark
import com.kxsv.schooldiary.di.util.AppDispatchers.IO
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.di.util.GradeNotification
import com.kxsv.schooldiary.di.util.GradeSummaryNotification
import com.kxsv.schooldiary.di.util.NotificationsConstants.GRADE_CHANNEL_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.NETWORK_CHANNEL_ID
import com.kxsv.schooldiary.util.Utils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

private const val TAG = "GradeSyncWorker"

@HiltWorker
class GradeSyncWorker @AssistedInject constructor(
	@Assisted private val appContext: Context,
	@Assisted workerParams: WorkerParameters,
	private val subjectTeacherRepository: SubjectTeacherRepository,
	private val subjectRepository: SubjectRepository,
	private val gradeRepository: GradeRepository,
	private val teacherRepository: TeacherRepository,
	@GradeSummaryNotification private val gradeSummaryNotificationBuilder: Notification.Builder,
	@GradeNotification private val gradeNotificationBuilder: Notification.Builder,
	private val notificationManager: NotificationManager,
	@Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : CoroutineWorker(appContext, workerParams) {
	
	companion object {
		fun startUpSyncWork() =
			PeriodicWorkRequestBuilder<DelegatingWorker>(360, TimeUnit.MINUTES)
				.setConstraints(SyncConstraints)
				.setInputData(GradeSyncWorker::class.delegatedData())
//				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.build()
	}
	
	override suspend fun getForegroundInfo(): ForegroundInfo =
		appContext.syncForegroundInfo()
	
	override suspend fun doWork(): Result {
		try {
			val fetchedGradesWithTeachers = Utils.measurePerformanceInMS(
				logger = { time, _ ->
					Log.i(
						TAG, "fetchRecentGradesWithTeachers: performance is" +
								" ${(time / 10f).roundToInt() / 100f} S"
					)
				}
			) {
				gradeRepository.fetchRecentGradesWithTeachers()
			}
			val newGradeEntities = Utils.measurePerformanceInMS(
				logger = { time, _ -> Log.i(TAG, "updateDatabase: performance is $time MS") }
			) {
				val fetchedGradesLocalized = mutableListOf<GradeWithSubject>()
				fetchedGradesWithTeachers.first.forEach {
					try {
						fetchedGradesLocalized.add(it.toGradeWithSubject(subjectRepository))
					} catch (e: NoSuchElementException) {
						Log.e(
							TAG,
							"updateTeachersDatabase: SchoolDiary: Couldn't localize grade ${it.mark}" +
									" on date ${it.date} for ${it.subjectAncestorName}.", e
						)
						Handler(Looper.getMainLooper()).post {
							Toast.makeText(
								appContext,
								"SchoolDiary: Couldn't localize grade ${it.mark} on date ${it.date} for ${it.subjectAncestorName}.\n" + e.message,
								Toast.LENGTH_LONG
							).show()
						}
					}
				}
				updateDatabase(fetchedGradesLocalized)
			}
			val newTeachers = Utils.measurePerformanceInMS(
				logger = { time, _ ->
					Log.i(
						TAG,
						"updateTeachersDatabase: performance is $time MS"
					)
				}
			) {
				updateTeachersDatabase(fetchedGradesWithTeachers.second)
			}
			
			if (ActivityCompat
					.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) ==
				PackageManager.PERMISSION_GRANTED
			) {
				Log.d(TAG, "DEBUG newGradeEntities: $newGradeEntities")
				if (newGradeEntities.isNotEmpty()) {
					notificationManager.notify(1, createSummaryNotification())
					newGradeEntities.forEach {
						notificationManager.notify(
							it.grade.gradeId, 2, createNotification(gradeEntity = it)
						)
					}
				}
				
			} else {
				// TODO: Consider calling
				//    ActivityCompat#requestPermissions
				// here to request the missing permissions, and then overriding
				//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
				//                                          int[] grantResults)
				// to handle the case where the user grants the permission. See the documentation
				// for ActivityCompat#requestPermissions for more details.
				
				return Result.success()
			}
			return Result.success()
		} catch (e: Exception) {
			when (e) {
				is NetworkException -> {
					Log.e(TAG, "fetchRecentGradesWithTeachers: you're not logged in", e)
					return Result.failure()
				}
				
				is IOException -> {
					Log.e(TAG, "fetchRecentGradesWithTeachers: exception on response parse", e)
					return Result.failure()
				}
				
				is TimeoutCancellationException -> {
					Log.e(TAG, "fetchRecentGradesWithTeachers: connection timed-out", e)
					return Result.retry()
					// TODO: show message that couldn't connect to site
				}
				
				else -> {
					Log.e(TAG, "fetchRecentGradesWithTeachers: exception", e)
					return Result.failure()
				}
			}
		}
	}
	
	private fun createSummaryNotification(): Notification {
		createNotificationChannel()
		return gradeSummaryNotificationBuilder.build()
	}
	
	private fun createNotification(gradeEntity: GradeWithSubject): Notification {
		createNotificationChannel()
		
		val text = Mark.getStringValueFrom(gradeEntity.grade.mark) + " | " + gradeEntity.grade.date
		return gradeNotificationBuilder.setContentText(text)
			.setContentTitle(gradeEntity.subject.getName()).build()
	}
	
	private fun createNotificationChannel() {
		notificationManager.createNotificationChannelGroup(
			NotificationChannelGroup(NETWORK_CHANNEL_ID, "Background sync")
		)
		val gradeChannel = NotificationChannel(
			GRADE_CHANNEL_ID,
			"Fetched Grades",
			NotificationManager.IMPORTANCE_DEFAULT
		).apply {
			description = "Notifies if new grades were fetched."
			group = NETWORK_CHANNEL_ID
		}
		
		notificationManager.createNotificationChannel(gradeChannel)
	}
	
	private suspend fun updateDatabase(fetchedGradeEntities: List<GradeWithSubject>): List<GradeWithSubject> {
		return withContext(ioDispatcher) {
			val newGradesFound: MutableList<GradeWithSubject> = mutableListOf()
			for (fetchedGradeEntity in fetchedGradeEntities) {
				val gradeId = fetchedGradeEntity.grade.gradeId
				val isGradeExisted = Utils.measurePerformanceInMS(
					{ time, result ->
						Log.d(
							TAG, "gradeDataSource.getById($gradeId): $time ms\n found = $result"
						)
					}
				) {
					gradeRepository.getGrade(gradeId) != null
				}
				gradeRepository.update(fetchedGradeEntity.grade)
				if (!isGradeExisted) {
					newGradesFound.add(fetchedGradeEntity)
					Log.i(TAG, "updateDatabase: FOUND NEW GRADE:\n${fetchedGradeEntity.grade}")
				}
			}
			newGradesFound
		}
	}
	
	private suspend fun updateTeachersDatabase(fetchedTeachers: Map<TeacherDto, Set<String>>) {
		return withContext(ioDispatcher) {
			fetchedTeachers.forEach { fetchedTeacher ->
				val fullName = fetchedTeacher.key.lastName.trim() + " " +
						fetchedTeacher.key.firstName.trim() + " " +
						fetchedTeacher.key.patronymic.trim()
				val teacherId = generateTeacherId(fullName)
				val existedTeacher = Utils.measurePerformanceInMS(
					{ time, result ->
						Log.e(
							TAG,
							"isTeacherExisted(${fetchedTeacher.key.lastName}): $time ms\n found = $result"
						)
					}
				) {
					teacherRepository.getById(teacherId)
				}
				val teacher = if (existedTeacher != null) {
					existedTeacher
				} else {
					val newTeacher = TeacherEntity(
						lastName = fetchedTeacher.key.lastName,
						firstName = fetchedTeacher.key.firstName,
						patronymic = fetchedTeacher.key.patronymic,
						phoneNumber = "",
						teacherId = teacherId
					)
					Log.d(
						TAG, "updateTeachersDatabase: upserted teacher(${newTeacher.lastName})"
					)
					teacherRepository.upsert(newTeacher)
					newTeacher
				}
				fetchedTeacher.value.forEach { subjectName ->
					try {
						val subject = subjectRepository.getSubjectByName(subjectName)
							?: throw NoSuchElementException("There is no saved subject with name $subjectName")
						
						subjectTeacherRepository.createSubjectTeacher(
							SubjectTeacher(
								subject.subjectId,
								teacherId
							)
						)
					} catch (e: NoSuchElementException) {
						Log.e(
							TAG,
							"updateTeachersDatabase: Failed link ${teacher.shortName()}) to $subjectName.",
							e
						)
						Handler(Looper.getMainLooper()).post {
							Toast.makeText(
								appContext,
								"SchoolDiary: Failed link ${teacher.shortName()}) to $subjectName." + e.message,
								Toast.LENGTH_LONG
							).show()
						}
					}
				}
			}
		}
	}
}