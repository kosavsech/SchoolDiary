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
	
	override suspend fun getAllByMasterId(studyDayId: Long): List<Schedule> {
		return scheduleDataSource.getAllByMasterId(studyDayId)
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
	
	override suspend fun upsertAll(schedules: List<Schedule>) {
		return scheduleDataSource.upsertAll(schedules)
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
	
	override suspend fun copyScheduleFromDate(fromDate: LocalDate, toDate: LocalDate) {
		val refStudyDay = studyDayDataSource.getByDate(fromDate)
			?: throw NoSuchElementException("StudyDay (date $fromDate) not found")
		val toStudyDay = studyDayDataSource.getByDate(toDate)
		
		val idOfDestinationDay = if (toStudyDay != null) {
			studyDayDataSource.upsert(toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId))
			toStudyDay.studyDayId
		} else {
			studyDayDataSource.upsert(refStudyDay.copy(date = toDate, studyDayId = 0))
		}
		
		toStudyDay?.studyDayId?.let {
			scheduleDataSource.deleteAllByDayId(it)
		}
		
		val copyOfSchedules: MutableList<Schedule> = mutableListOf()
		scheduleDataSource.getAllByMasterId(refStudyDay.studyDayId).forEach {
			copyOfSchedules.add(it.copy(studyDayMasterId = idOfDestinationDay, scheduleId = 0))
		}
		scheduleDataSource.upsertAll(copyOfSchedules)
	}
	
	override suspend fun copyScheduleFromId(refStudyDayId: Long, toDate: LocalDate) {
		val refStudyDay = studyDayDataSource.getById(refStudyDayId)
			?: throw NoSuchElementException("StudyDay (id $refStudyDayId) not found")
		
		val copyOfStudyDay = refStudyDay.copy(date = toDate, studyDayId = 0)
		val copyOfSchedules: MutableList<Schedule> = mutableListOf()
		val idOfStudyDayCopy = studyDayDataSource.upsert(copyOfStudyDay)
		
		scheduleDataSource.getAllByMasterId(refStudyDay.studyDayId).forEach {
			copyOfSchedules.add(it.copy(studyDayMasterId = idOfStudyDayCopy))
		}
		scheduleDataSource.upsertAll(copyOfSchedules)
	}
	
	override suspend fun copyScheduleFromDateToId(fromDate: LocalDate, toStudyDayId: Long) {
		val refStudyDay = studyDayDataSource.getByDate(fromDate)
			?: throw NoSuchElementException("Reference StudyDay (date $fromDate) not found")
		val toStudyDay = studyDayDataSource.getById(toStudyDayId)
			?: throw NoSuchElementException("Destination StudyDay (id $toStudyDayId) not found")
		
		val updatedStudyDay = toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
		studyDayDataSource.upsert(updatedStudyDay)
		scheduleDataSource.deleteAllByDayId(toStudyDay.studyDayId)
		
		val copyOfSchedules: MutableList<Schedule> = mutableListOf()
		scheduleDataSource.getAllByMasterId(refStudyDay.studyDayId).forEach {
			copyOfSchedules.add(it.copy(studyDayMasterId = toStudyDay.studyDayId))
		}
		scheduleDataSource.upsertAll(copyOfSchedules)
	}
	
	override suspend fun copyScheduleFromIdToId(
		refStudyDayId: Long,
		toStudyDayId: Long,
	) {
		val refStudyDay = studyDayDataSource.getById(refStudyDayId)
			?: throw NoSuchElementException("Reference StudyDay (id $refStudyDayId) not found")
		val toStudyDay = studyDayDataSource.getById(toStudyDayId)
			?: throw NoSuchElementException("Destination StudyDay (id $toStudyDayId) not found")
		
		val updatedStudyDay = toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
		studyDayDataSource.upsert(updatedStudyDay)
		scheduleDataSource.deleteAllByDayId(toStudyDay.studyDayId)
		
		val copyOfSchedules: MutableList<Schedule> = mutableListOf()
		scheduleDataSource.getAllByMasterId(refStudyDay.studyDayId).forEach {
			copyOfSchedules.add(it.copy(studyDayMasterId = toStudyDay.studyDayId))
		}
		scheduleDataSource.upsertAll(copyOfSchedules)
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