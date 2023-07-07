package com.kxsv.schooldiary.data.features.schedule

import com.kxsv.schooldiary.data.features.study_day.StudyDay
import com.kxsv.schooldiary.data.features.study_day.StudyDayDao
import com.kxsv.schooldiary.domain.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
	private val scheduleDataSource: ScheduleDao,
	private val studyDayDataSource: StudyDayDao,
	//@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : ScheduleRepository {
	
	override fun getSchedulesStream(): Flow<List<Schedule>> {
		return scheduleDataSource.observeAll()
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
	
	override suspend fun getScheduleWithStudyDay(scheduleId: Long): ScheduleWithStudyDay? {
		return scheduleDataSource.getByIdWithStudyDay(scheduleId)
	}
	
	override suspend fun createSchedule(schedule: Schedule, date: LocalDate) {
		val saveToStudyDay = studyDayDataSource.getByDate(date)
		val newSchedule = if (saveToStudyDay == null) {
			val studyDayId = studyDayDataSource.upsert(StudyDay(date))
			schedule.copy(studyDayMasterId = studyDayId)
		} else {
			schedule.copy(studyDayMasterId = saveToStudyDay.studyDayId)
		}
		scheduleDataSource.upsert(newSchedule)
	}
	
	override suspend fun updateSchedule(schedule: Schedule, date: LocalDate) {
		val saveToStudyDay = studyDayDataSource.getByDate(date)
		val newSchedule = if (saveToStudyDay == null) {
			val studyDayId = studyDayDataSource.upsert(StudyDay(date))
			schedule.copy(studyDayMasterId = studyDayId)
		} else {
			schedule.copy(studyDayMasterId = saveToStudyDay.studyDayId)
		}
		scheduleDataSource.upsert(newSchedule)
	}
	
	override suspend fun deleteAllSchedules() {
		scheduleDataSource.deleteAll()
	}
	
	override suspend fun deleteSchedule(scheduleId: Long) {
		scheduleDataSource.deleteById(scheduleId)
	}
	
}