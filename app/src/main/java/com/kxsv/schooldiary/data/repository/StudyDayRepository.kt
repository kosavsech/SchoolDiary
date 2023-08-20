package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayWithSchedulesAndSubjects
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface StudyDayRepository {
	
	fun observeStudyDays(): Flow<List<StudyDayEntity>>
	
	fun observeStudyDay(studyDayId: Long): Flow<StudyDayEntity>
	
	suspend fun getAll(): List<StudyDayEntity>
	
	suspend fun getById(studyDayId: Long): StudyDayEntity?
	
	suspend fun getByDate(date: LocalDate): StudyDayEntity?
	
	suspend fun getDayAndSchedulesWithSubjectsByDate(date: LocalDate): StudyDayWithSchedulesAndSubjects?
	
	/**
	 * Get date for next lesson of subject
	 *
	 * @param subjectId
	 * @param startDate not included in search range
	 * @return
	 */
	suspend fun getDateForNextLessonOfSubject(subjectId: String, startDate: LocalDate): LocalDate?
	
	suspend fun create(studyDay: StudyDayEntity): Long
	
	suspend fun update(studyDay: StudyDayEntity)
	
	suspend fun deleteAllStudyDays()
	
	suspend fun deleteStudyDay(studyDayId: Long)
}