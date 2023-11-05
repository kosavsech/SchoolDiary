package com.kxsv.schooldiary.app.sync.workers

import android.Manifest
import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity
import com.kxsv.schooldiary.data.mapper.toGradeWithSubject
import com.kxsv.schooldiary.data.remote.dtos.TeacherDto
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.SubjectTeacherRepository
import com.kxsv.schooldiary.data.repository.TeacherRepository
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateSubjectId
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateTeacherId
import com.kxsv.schooldiary.data.util.Mark
import com.kxsv.schooldiary.di.util.AppDispatchers.IO
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.di.util.GradeNotification
import com.kxsv.schooldiary.di.util.GradeSummaryNotification
import com.kxsv.schooldiary.di.util.NotificationsConstants.GRADE_CHANNEL_ID
import com.kxsv.schooldiary.di.util.NotificationsConstants.NETWORK_CHANNEL_GROUP_ID
import com.kxsv.schooldiary.util.PERMISSION_REQUEST_CODE
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.isPermissionGranted
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import org.jsoup.UnsupportedMimeTypeException
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
			PeriodicWorkRequestBuilder<DelegatingWorker>(60, TimeUnit.MINUTES)
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
									" on date ${it.date} for ${it.subjectAncestorFullName}.", e
						)
						Handler(Looper.getMainLooper()).post {
							Toast.makeText(
								appContext,
								"SchoolDiary: Couldn't localize grade ${it.mark} on date ${it.date} for ${it.subjectAncestorFullName}.\n" + e.message,
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
			
			if (appContext.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)) {
				Log.d(TAG, "DEBUG newGradeEntities: $newGradeEntities")
				if (newGradeEntities.isNotEmpty()) {
					notificationManager.notify(1, createSummaryNotification())
					newGradeEntities.forEach {
						notificationManager.notify(
							it.grade.gradeId,
							2,
							createNotification(gradeEntity = it)
						)
					}
				}
				return Result.success()
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
					ActivityCompat.requestPermissions(
						appContext as Activity,
						arrayOf(Manifest.permission.POST_NOTIFICATIONS),
						PERMISSION_REQUEST_CODE
					)
				}
				return Result.failure()
			}
		} catch (e: NetworkException) {
			Log.e(TAG, "fetchRecentGradesWithTeachers: NetworkException on fetch", e)
			return Result.failure()
		} catch (e: TimeoutCancellationException) {
			Log.e(TAG, "fetchRecentGradesWithTeachers: connection timed-out", e)
			// TODO: show message that couldn't connect to site
			return Result.retry()
		} catch (e: java.net.MalformedURLException) {
			return Result.failure()
		} catch (e: HttpStatusException) {
			Log.e(TAG, "fetchRecentGradesWithTeachers: exception on connect")
			return Result.failure()
		} catch (e: UnsupportedMimeTypeException) {
			return Result.failure()
		} catch (e: java.net.SocketTimeoutException) {
			return Result.failure()
		} catch (e: IOException) {
			Log.e(TAG, "fetchRecentGradesWithTeachers: exception on response parseTermRows", e)
			return Result.failure()
		} catch (e: Exception) {
			Log.e(TAG, "fetchRecentGradesWithTeachers: exception", e)
			return Result.failure()
		}
	}
	
	private fun createSummaryNotification(): Notification {
		createNotificationChannel()
		return gradeSummaryNotificationBuilder.build()
	}
	
	private fun createNotification(gradeEntity: GradeWithSubject): Notification {
		createNotificationChannel()
		
		val text =
			Mark.getStringValueFrom(gradeEntity.grade.mark) + " | " + gradeEntity.grade.date.format(
				Utils.monthDayDateFormatter
			)
		return gradeNotificationBuilder.setContentText(text)
			.setContentTitle(gradeEntity.subject.getName()).build()
	}
	
	private fun createNotificationChannel() {
		notificationManager.createNotificationChannelGroup(
			NotificationChannelGroup(NETWORK_CHANNEL_GROUP_ID, "Background sync")
		)
		val gradeChannel = NotificationChannel(
			GRADE_CHANNEL_ID,
			"Fetched Grades",
			NotificationManager.IMPORTANCE_DEFAULT
		).apply {
			description = "Notifies if new grades were fetched."
			group = NETWORK_CHANNEL_GROUP_ID
		}
		
		notificationManager.createNotificationChannel(gradeChannel)
	}
	
	private fun GradeEntity.isContentEqual(gradeEntity: GradeEntity): Boolean {
		if (this.gradeId != gradeEntity.gradeId) return false
		if (this.mark != gradeEntity.mark) return false
		if (this.typeOfWork != gradeEntity.typeOfWork) return false
		return true
	}
	
	private suspend fun updateDatabase(fetchedGradeEntities: List<GradeWithSubject>): List<GradeWithSubject> {
		return withContext(ioDispatcher) {
			val newGradesFound: MutableList<GradeWithSubject> = mutableListOf()
			fetchedGradeEntities.groupBy { it.grade.date }.forEach { dateAndFetchedGrades ->
				val savedGradesOnDate = gradeRepository.getGradesByDate(dateAndFetchedGrades.key)
				savedGradesOnDate.forEach { localGrade ->
					val relevantFetchedGrade = dateAndFetchedGrades.value.run {
						val index = this
							.map { it.grade }
							.sortedBy { it.gradeId }
							.binarySearchBy(localGrade.gradeId) { it.gradeId }
						if (index != -1) {
							return@run this[index]
						} else {
							return@run null
						}
					}
					if (relevantFetchedGrade == null || !localGrade.isContentEqual(
							relevantFetchedGrade.grade
						)
					) {
						gradeRepository.deleteGrade(localGrade.gradeId)
					}
				}
				dateAndFetchedGrades.value.forEach { fetchedGradeEntity ->
					val isGradeExisted =
						gradeRepository.getGrade(fetchedGradeEntity.grade.gradeId) != null
					if (!isGradeExisted) {
						gradeRepository.upsert(fetchedGradeEntity.grade)
						newGradesFound.add(fetchedGradeEntity)
						Log.i(
							TAG, "updateDatabase: FOUND NEW GRADE:" +
									"\n${fetchedGradeEntity.grade} ${fetchedGradeEntity.grade.date}\n" +
									fetchedGradeEntity.subject.getName()
						)
					}
				}
			}
			return@withContext newGradesFound
		}
	}
	
	private suspend fun updateTeachersDatabase(fetchedTeachers: Map<TeacherDto, Set<String>>) {
		return withContext(ioDispatcher) {
			fetchedTeachers.forEach { fetchedTeacher ->
				val fullName = fetchedTeacher.key.lastName.trim() + " " +
						fetchedTeacher.key.firstName.trim() + " " +
						fetchedTeacher.key.patronymic.trim()
				
				val teacherId = generateTeacherId(fullName)
				val isTeacherExisted = teacherRepository.getById(teacherId) != null
				
				if (!isTeacherExisted) {
					val newTeacher = TeacherEntity(
						lastName = fetchedTeacher.key.lastName,
						firstName = fetchedTeacher.key.firstName,
						patronymic = fetchedTeacher.key.patronymic,
						phoneNumber = "",
						teacherId = teacherId
					)
					teacherRepository.upsert(newTeacher)
					Log.d(
						TAG, "updateTeachersDatabase: FOUND NEW TEACHER(${newTeacher.lastName})"
					)
				}
				fetchedTeacher.value.forEach { subjectName ->
					subjectTeacherRepository.upsert(
						SubjectTeacher(generateSubjectId(subjectName), teacherId)
					)
				}
			}
		}
	}
}