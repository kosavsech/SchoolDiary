package com.kxsv.schooldiary.data.features.schedule

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDataSource: ScheduleDao,
    //@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : ScheduleRepository {

    override fun getSchedulesStream(): Flow<List<Schedule>> {
        return scheduleDataSource.observeAll()
    }

    override fun getSchedulesWithSubjectByDateStream(date: LocalDate): Flow<List<ScheduleWithSubject>> {
        return scheduleDataSource.observeAllWithSubjectByDate(date)
    }

    override fun getScheduleStream(scheduleId: Long): Flow<Schedule> {
        return scheduleDataSource.observeById(scheduleId)
    }

    override suspend fun getSchedules(): List<Schedule> {
        return scheduleDataSource.getAll()
    }

    override suspend fun getSchedule(scheduleId: Long): Schedule? {
        return scheduleDataSource.getById(scheduleId)
    }

    override suspend fun getScheduleWithSubject(scheduleId: Long): ScheduleWithSubject? {
        return scheduleDataSource.getByIdWithSubject(scheduleId)
    }

    override suspend fun createSchedule(schedule: Schedule) {
        scheduleDataSource.upsert(schedule)
    }

    override suspend fun updateSchedule(schedule: Schedule) {
        scheduleDataSource.upsert(schedule)
    }

    override suspend fun deleteAllSchedules() {
        scheduleDataSource.deleteAll()
    }

    override suspend fun deleteSchedule(scheduleId: Long) {
        scheduleDataSource.deleteById(scheduleId)
    }

}