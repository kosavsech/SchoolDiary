package com.kxsv.schooldiary.domain

import com.kxsv.schooldiary.data.features.schedule.Schedule
import com.kxsv.schooldiary.data.features.schedule.ScheduleWithStudyDay
import com.kxsv.schooldiary.data.features.schedule.ScheduleWithSubject
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ScheduleRepository {
	
	fun getSchedulesStream(): Flow<List<Schedule>>
	
	fun getScheduleStream(scheduleId: Long): Flow<Schedule>
	
	suspend fun getSchedules(): List<Schedule>
	
	suspend fun getSchedule(scheduleId: Long): Schedule?
	
	suspend fun getScheduleWithSubject(scheduleId: Long): ScheduleWithSubject?
	
	suspend fun getScheduleWithStudyDay(scheduleId: Long): ScheduleWithStudyDay?
	
	suspend fun createSchedule(schedule: Schedule, date: LocalDate)
	
	suspend fun updateSchedule(schedule: Schedule, date: LocalDate)
	
	suspend fun copyScheduleFromDate(fromDate: LocalDate, toDate: LocalDate)
	
	suspend fun copyScheduleFromStudyDayId(refStudyDayId: Long, toDate: LocalDate)
	
	suspend fun deleteAllSchedules()
	
	suspend fun deleteSchedule(scheduleId: Long)
}