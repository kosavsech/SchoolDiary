package com.kxsv.schooldiary.data.local.features.schedule

import com.kxsv.schooldiary.data.local.features.study_day.StudyDay
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.domain.ScheduleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
	private val scheduleDataSource: ScheduleDao,
	private val studyDayDataSource: StudyDayDao,
	@IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ScheduleRepository {
	
	override fun getSchedulesStream(): Flow<List<Schedule>> {
		return scheduleDataSource.observeAll()
	}
	
	override fun getScheduleStream(scheduleId: Long): Flow<Schedule> {
		return scheduleDataSource.observeById(scheduleId)
	}
	
	override suspend fun getAll(): List<Schedule> {
		return scheduleDataSource.getAll()
	}
	
	override suspend fun getAllByMasterId(studyDayId: Long): List<Schedule> {
		return scheduleDataSource.getAllByMasterId(studyDayId)
	}
	
	override suspend fun getSchedule(scheduleId: Long): Schedule? {
		return scheduleDataSource.getById(scheduleId)
	}
	
	override suspend fun getByIdAndIndex(studyDayMasterId: Long, index: Int): Schedule? {
		return scheduleDataSource.getByIdAndIndex(studyDayMasterId, index)
	}
	
	override suspend fun getScheduleWithSubject(scheduleId: Long): ScheduleWithSubject? {
		return scheduleDataSource.getByIdWithSubject(scheduleId)
	}
	
	override suspend fun getScheduleWithStudyDay(scheduleId: Long): ScheduleWithStudyDay? {
		return scheduleDataSource.getByIdWithStudyDay(scheduleId)
	}
	
	override suspend fun upsertAll(schedules: List<Schedule>) {
		return scheduleDataSource.upsertAll(schedules)
	}
	
	override suspend fun createSchedule(schedule: Schedule, date: LocalDate) {
		val studyDayMaster = studyDayDataSource.getByDate(date)
		val newSchedule = if (studyDayMaster == null) {
			val studyDayId = studyDayDataSource.upsert(StudyDay(date))
			schedule.copy(studyDayMasterId = studyDayId)
		} else {
			schedule.copy(studyDayMasterId = studyDayMaster.studyDayId)
		}
		scheduleDataSource.upsert(newSchedule)
	}
	
	override suspend fun updateSchedule(schedule: Schedule, date: LocalDate) {
		val studyDayMaster = studyDayDataSource.getByDate(date)
		val updatedSchedule = if (studyDayMaster == null) {
			val studyDayId = studyDayDataSource.upsert(StudyDay(date))
			schedule.copy(studyDayMasterId = studyDayId)
		} else {
			schedule.copy(studyDayMasterId = studyDayMaster.studyDayId)
		}
		scheduleDataSource.upsert(updatedSchedule)
	}
	
	override suspend fun deleteAllSchedules() {
		scheduleDataSource.deleteAll()
	}
	
	override suspend fun deleteAllByDayId(studyDayMasterId: Long) {
		scheduleDataSource.deleteAllByDayId(studyDayMasterId)
	}
	
	override suspend fun deleteSchedule(scheduleId: Long) {
		scheduleDataSource.deleteById(scheduleId)
	}
	
}