package com.kxsv.schooldiary.data.features.schedule

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ScheduleRepository {

    fun getSchedulesStream(): Flow<List<Schedule>>

    fun getSchedulesWithSubjectByDateStream(date: LocalDate): Flow<List<ScheduleWithSubject>>

    fun getScheduleStream(scheduleId: Long): Flow<Schedule>

    suspend fun getSchedules(): List<Schedule>

    suspend fun getSchedule(scheduleId: Long): Schedule?

    suspend fun getScheduleWithSubject(scheduleId: Long): ScheduleWithSubject?

    suspend fun createSchedule(schedule: Schedule)

    suspend fun updateSchedule(schedule: Schedule)

    suspend fun deleteAllSchedules()

    suspend fun deleteSchedule(scheduleId: Long)
}