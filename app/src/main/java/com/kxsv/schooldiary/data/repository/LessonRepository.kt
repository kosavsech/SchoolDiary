package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithStudyDay
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.remote.dtos.LessonDto
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.util.ScheduleCompareResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LessonRepository {
	
	fun observeAll(): Flow<List<LessonEntity>>
	
	fun observeById(lessonId: Long): Flow<LessonEntity>
	
	fun observeAllWithSubjectForDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Flow<Map<LocalDate, List<LessonWithSubject>>>
	
	suspend fun getAll(): List<LessonEntity>
	
	suspend fun getDateAndLessonsWithSubjectByDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Map<LocalDate, List<LessonWithSubject>>
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws java.io.IOException if couldn't parseTermRows document
	 */
	suspend fun fetchLessonsOnDate(localDate: LocalDate): List<LessonDto>
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 * @throws NetworkException.PageNotFound
	 * @throws java.io.IOException if couldn't parseTermRows document
	 */
	suspend fun fetchSoonSchedule(): Map<LocalDate, ScheduleCompareResult>
	
	suspend fun getAllByMasterId(studyDayId: Long): List<LessonEntity>
	
	suspend fun getLesson(lessonId: Long): LessonEntity?
	
	suspend fun getByIdAndIndex(studyDayMasterId: Long, index: Int): LessonEntity?
	
	suspend fun getLessonWithSubject(lessonId: Long): LessonWithSubject?
	
	suspend fun getLessonWithStudyDay(lessonId: Long): LessonWithStudyDay?
	
	suspend fun upsertAll(lessons: List<LessonEntity>)
	
	suspend fun createLesson(lesson: LessonEntity, date: LocalDate)
	
	suspend fun updateLesson(lesson: LessonEntity, date: LocalDate)
	
	suspend fun deleteAllLessons()
	
	suspend fun deleteAllByDayId(studyDayMasterId: Long)
	
	suspend fun deleteLesson(lessonId: Long)
}