package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithStudyDay
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.remote.lesson.LessonDto
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface LessonRepository {
	
	fun getLessonsStream(): Flow<List<LessonEntity>>
	
	fun getLessonStream(lessonId: Long): Flow<LessonEntity>
	
	suspend fun getAll(): List<LessonEntity>
	
	suspend fun fetchLessonsByDate(localDate: LocalDate): List<LessonDto>
	
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