package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.local.features.study_day.StudyDay
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayWithSchedulesAndSubjects
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface StudyDayRepository {
	
	fun getStudyDaysStream(): Flow<List<StudyDay>>
	
	fun getStudyDayStream(studyDayId: Long): Flow<StudyDay>
	
	suspend fun getAll(): List<StudyDay>
	
	suspend fun getById(studyDayId: Long): StudyDay?
	
	suspend fun getByDate(date: LocalDate): StudyDay?
	
	suspend fun getDayAndSchedulesWithSubjectsByDate(date: LocalDate): StudyDayWithSchedulesAndSubjects?
	
	suspend fun create(studyDay: StudyDay): Long
	
	suspend fun update(studyDay: StudyDay)
	
	suspend fun deleteAllStudyDays()
	
	suspend fun deleteStudyDay(studyDayId: Long)
}