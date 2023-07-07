package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.features.study_day.StudyDay
import com.kxsv.schooldiary.data.features.study_day.StudyDayWithSchedulesAndSubjects
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface StudyDayRepository {
	
	fun getStudyDaysStream(): Flow<List<StudyDay>>
	
	fun getStudyDayStream(studyDayId: Long): Flow<StudyDay>
	
	suspend fun getStudyDays(): List<StudyDay>
	
	suspend fun getStudyDay(studyDayId: Long): StudyDay?
	
	suspend fun getStudyDayByDate(date: LocalDate): StudyDay?
	
	suspend fun getDayAndSchedulesWithSubjectsByDate(date: LocalDate): StudyDayWithSchedulesAndSubjects?
	
	suspend fun createStudyDay(studyDay: StudyDay): Long
	
	suspend fun updateStudyDay(studyDay: StudyDay)
	
	suspend fun deleteAllStudyDays()
	
	suspend fun deleteStudyDay(studyDayId: Long)
}