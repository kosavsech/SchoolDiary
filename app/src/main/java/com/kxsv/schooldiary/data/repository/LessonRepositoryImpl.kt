package com.kxsv.schooldiary.data.repository

import android.util.Log
import com.kxsv.schooldiary.data.local.features.lesson.LessonDao
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithStudyDay
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.mapper.save
import com.kxsv.schooldiary.data.mapper.toSubjectEntitiesIndexed
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.dtos.LessonDto
import com.kxsv.schooldiary.data.remote.parsers.LessonParser
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.toList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.set

private const val TAG = "LessonRepositoryImpl"

@Singleton
class LessonRepositoryImpl @Inject constructor(
	private val lessonDataSource: LessonDao,
	private val webService: WebService,
	private val studyDayDataSource: StudyDayDao,
	private val subjectDataSource: SubjectDao,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : LessonRepository {
	
	override fun observeAll(): Flow<List<LessonEntity>> {
		return lessonDataSource.observeAll()
	}
	
	override fun observeById(lessonId: Long): Flow<LessonEntity> {
		return lessonDataSource.observeById(lessonId)
	}
	
	override fun observeAllWithSubjectForDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Flow<Map<LocalDate, List<LessonWithSubject>>> {
		return lessonDataSource.observeAllWithSubjectForDateRange(startRange, endRange)
	}
	
	override suspend fun getAll(): List<LessonEntity> {
		return lessonDataSource.getAll()
	}
	
	override suspend fun getDateAndLessonsWithSubjectByDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Map<LocalDate, List<LessonWithSubject>> {
		return lessonDataSource.getDayAndLessonsWithSubjectByDateRange(startRange, endRange)
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	override suspend fun fetchLessonsOnDate(localDate: LocalDate): List<LessonDto> {
		val dayInfo = webService.getDayInfo(localDate)
		return LessonParser().parse(dayInfo, localDate)
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	override suspend fun fetchSoonSchedule(): Map<LocalDate, Utils.ScheduleCompareResult> {
		return withContext(ioDispatcher) {
			withTimeout(15000L) {
				val startRange = Utils.currentDate
				val endRange = startRange.plusDays(14)
				val newSchedules: MutableMap<LocalDate, Utils.ScheduleCompareResult> =
					mutableMapOf()
				
				(startRange..endRange).toList().forEach { date ->
					if (date.dayOfWeek == DayOfWeek.SUNDAY) return@forEach
					@Suppress("DeferredResultUnused")
					async {
						val compareResult = fetchCompareSchedule(date = date)
						if (compareResult != null) newSchedules[date] = compareResult
					}
				}
				
				return@withTimeout newSchedules
			}
		}
	}
	
	/**
	 * Fetch schedule on date
	 *
	 * @param date
	 * @return [Utils.ScheduleCompareResult]([isNew][Utils.ScheduleCompareResult.isNew] = [true][Boolean])
	 * if no local schedule on [date], [ScheduleCompareResult]([isSameAs][com.kxsv.schooldiary.data.repository.LessonRepositoryImpl.ScheduleCompareResult.isSameAs] = [true][Boolean])
	 * if local schedule is equal to fetched, ([isSameAs][Utils.ScheduleCompareResult.isDifferent] = [false][Boolean]),
	 * if not
	 */
	private suspend fun fetchCompareSchedule(date: LocalDate): Utils.ScheduleCompareResult? {
		return withContext(ioDispatcher) {
			val dayInfo = async { webService.getDayInfo(date) }
			
			val localSchedule = async {
				val mappedLocalSchedule = mutableMapOf<Int, SubjectEntity>()
				lessonDataSource.getAllWithSubjectByDate(date).forEach {
					mappedLocalSchedule[it.lesson.index] = it.subject
				}
				mappedLocalSchedule
			}
			val remoteSchedule = async {
				LessonParser()
					.parse(dayInfo = dayInfo.await(), localDate = date)
			}
			
			if (localSchedule.await().isEmpty()) {
				Log.d(TAG, "fetchCompareSchedule: new schedule saved for date = $date")
				remoteSchedule.await().save(lessonDataSource, subjectDataSource, studyDayDataSource)
				return@withContext Utils.ScheduleCompareResult(isNew = true, isDifferent = false)
			} else {
				val local = localSchedule.await()
				val remoteIndexed = remoteSchedule.await()
					.toSubjectEntitiesIndexed(subjectDataSource, studyDayDataSource)
				
				if (local.isSameAs(remoteIndexed)) {
					Log.d(TAG, "fetchCompareSchedule: schedule is same for date = $date")
					return@withContext null
				} else {
					Log.d(TAG, "fetchCompareSchedule: schedule is different for date = $date")
					return@withContext Utils.ScheduleCompareResult(
						isNew = false,
						isDifferent = true
					)
				}
				
			}
		}
	}
	
	private fun Map<Int, SubjectEntity>.isSameAs(other: Map<Int, SubjectEntity>?): Boolean {
		if (other === this) return true
		if (other !is Map<*, *>) return false
		if (size != other.size) return false
		
		return other.entries.all { lessonFromOther ->
			this.containsLesson(lessonFromOther)
		}
	}
	
	private fun Map<Int, SubjectEntity>.containsLesson(entry: Map.Entry<Int, SubjectEntity>?): Boolean {
		if (entry !is Map.Entry<*, *>) return false
		val key = entry.key
		val value = entry.value
		val ourValue = get(key)
		
		if (value != ourValue) {
			return false
		}
		
		return true
	}
	
	override suspend fun getAllByMasterId(studyDayId: Long): List<LessonEntity> {
		return lessonDataSource.getAllByMasterId(studyDayId)
	}
	
	override suspend fun getLesson(lessonId: Long): LessonEntity? {
		return lessonDataSource.getById(lessonId)
	}
	
	override suspend fun getByIdAndIndex(studyDayMasterId: Long, index: Int): LessonEntity? {
		return lessonDataSource.getByIdAndIndex(studyDayMasterId, index)
	}
	
	override suspend fun getLessonWithSubject(lessonId: Long): LessonWithSubject? {
		return lessonDataSource.getByIdWithSubject(lessonId)
	}
	
	override suspend fun getLessonWithStudyDay(lessonId: Long): LessonWithStudyDay? {
		return lessonDataSource.getByIdWithStudyDay(lessonId)
	}
	
	override suspend fun upsertAll(lessons: List<LessonEntity>) {
		return lessonDataSource.upsertAll(lessons)
	}
	
	override suspend fun createLesson(lesson: LessonEntity, date: LocalDate) {
		val studyDayMaster = studyDayDataSource.getByDate(date)
		val newSchedule = if (studyDayMaster == null) {
			val studyDayId = studyDayDataSource.upsert(StudyDayEntity(date))
			lesson.copy(studyDayMasterId = studyDayId)
		} else {
			lesson.copy(studyDayMasterId = studyDayMaster.studyDayId)
		}
		lessonDataSource.upsert(newSchedule)
	}
	
	override suspend fun updateLesson(lesson: LessonEntity, date: LocalDate) {
		val studyDayMaster = studyDayDataSource.getByDate(date)
		val lessonWithMasterId = if (studyDayMaster == null) {
			val studyDayId = studyDayDataSource.upsert(StudyDayEntity(date))
			lesson.copy(studyDayMasterId = studyDayId)
		} else {
			lesson.copy(studyDayMasterId = studyDayMaster.studyDayId)
		}
		lessonDataSource.upsert(lessonWithMasterId)
	}
	
	override suspend fun deleteAllLessons() {
		lessonDataSource.deleteAll()
	}
	
	override suspend fun deleteAllByDayId(studyDayMasterId: Long) {
		lessonDataSource.deleteAllByDayId(studyDayMasterId)
	}
	
	override suspend fun deleteLesson(lessonId: Long) {
		lessonDataSource.deleteById(lessonId)
	}
	
}