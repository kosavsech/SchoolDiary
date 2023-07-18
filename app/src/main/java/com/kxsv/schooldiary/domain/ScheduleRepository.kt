package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.local.features.schedule.Schedule
import com.kxsv.schooldiary.data.local.features.schedule.ScheduleWithStudyDay
import com.kxsv.schooldiary.data.local.features.schedule.ScheduleWithSubject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ScheduleRepository {
	
	fun getSchedulesStream(): Flow<List<Schedule>>
	
	fun getScheduleStream(scheduleId: Long): Flow<Schedule>
	
	suspend fun getAll(): List<Schedule>
	
	suspend fun getAllByMasterId(studyDayId: Long): List<Schedule>
	
	suspend fun getSchedule(scheduleId: Long): Schedule?
	
	suspend fun getByIdAndIndex(studyDayMasterId: Long, index: Int): Schedule?
	
	suspend fun getScheduleWithSubject(scheduleId: Long): ScheduleWithSubject?
	
	suspend fun getScheduleWithStudyDay(scheduleId: Long): ScheduleWithStudyDay?
	
	suspend fun upsertAll(schedules: List<Schedule>)
	
	suspend fun createSchedule(schedule: Schedule, date: LocalDate)
	
	suspend fun updateSchedule(schedule: Schedule, date: LocalDate)
	
	suspend fun deleteAllSchedules()
	
	suspend fun deleteAllByDayId(studyDayMasterId: Long)
	
	suspend fun deleteSchedule(scheduleId: Long)
}