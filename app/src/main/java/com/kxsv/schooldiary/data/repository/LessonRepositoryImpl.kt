package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.lesson.LessonDao
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithStudyDay
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.lesson.LessonDto
import com.kxsv.schooldiary.data.remote.lesson.ScheduleParser
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LessonRepositoryImpl @Inject constructor(
	private val lessonDataSource: LessonDao,
	private val webService: WebService,
	private val studyDayDataSource: StudyDayDao,
//	@IoDispatcher private val dispatcher: CoroutineDispatcher,
) : LessonRepository {
	
	override fun getLessonsStream(): Flow<List<LessonEntity>> {
		return lessonDataSource.observeAll()
	}
	
	override fun getLessonStream(lessonId: Long): Flow<LessonEntity> {
		return lessonDataSource.observeById(lessonId)
	}
	
	override suspend fun getAll(): List<LessonEntity> {
		return lessonDataSource.getAll()
	}
	
	override suspend fun fetchLessonsByDate(localDate: LocalDate): List<LessonDto> {
		val lessons = webService.getScheduleForDate(localDate)
		return ScheduleParser().parse(lessons, localDate)
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
		val updatedSchedule = if (studyDayMaster == null) {
			val studyDayId = studyDayDataSource.upsert(StudyDayEntity(date))
			lesson.copy(studyDayMasterId = studyDayId)
		} else {
			lesson.copy(studyDayMasterId = studyDayMaster.studyDayId)
		}
		lessonDataSource.upsert(updatedSchedule)
	}
	
	override suspend fun deleteAllLessons() {
		lessonDataSource.deleteAll()
	}
	
	override suspend fun deleteAllByDayId(studyDayMasterId: Long) {
		lessonDataSource.deleteAllByDayId(studyDayMasterId)
	}
	
	override suspend fun deleteLesson(scheduleId: Long) {
		lessonDataSource.deleteById(scheduleId)
	}
	
}