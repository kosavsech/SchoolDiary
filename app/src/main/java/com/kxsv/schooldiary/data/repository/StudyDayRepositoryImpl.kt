package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.study_day.StudyDayDao
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayWithSchedulesAndSubjects
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyDayRepositoryImpl @Inject constructor(
	private val studyDayDataSource: StudyDayDao,
	//@Dispatcher(AppDispatchers.Default) private val dispatcher: CoroutineDispatcher,
) : StudyDayRepository {
	
	override fun observeStudyDays(): Flow<List<StudyDayEntity>> {
		return studyDayDataSource.observeAll()
	}
	
	override fun observeStudyDay(studyDayId: Long): Flow<StudyDayEntity> {
		return studyDayDataSource.observeById(studyDayId)
	}
	
	override suspend fun getAll(): List<StudyDayEntity> {
		return studyDayDataSource.getAll()
	}
	
	override suspend fun getById(studyDayId: Long): StudyDayEntity? {
		return studyDayDataSource.getById(studyDayId)
	}
	
	override suspend fun getByDate(date: LocalDate): StudyDayEntity? {
		return studyDayDataSource.getByDate(date)
	}
	
	override suspend fun getDayAndSchedulesWithSubjectsByDate(date: LocalDate): StudyDayWithSchedulesAndSubjects? {
		return studyDayDataSource.getByDateWithSchedulesAndSubjects(date)
	}
	
	override suspend fun create(studyDay: StudyDayEntity): Long {
		return studyDayDataSource.upsert(studyDay)
	}
	
	override suspend fun update(studyDay: StudyDayEntity) {
		studyDayDataSource.upsert(studyDay)
	}
	
	override suspend fun deleteAllStudyDays() {
		studyDayDataSource.deleteAll()
	}
	
	override suspend fun deleteStudyDay(studyDayId: Long) {
		studyDayDataSource.deleteById(studyDayId)
	}
	
}