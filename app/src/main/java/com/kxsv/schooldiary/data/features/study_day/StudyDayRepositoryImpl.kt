package com.kxsv.schooldiary.data.features.study_day

import com.kxsv.schooldiary.domain.StudyDayRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudyDayRepositoryImpl @Inject constructor(
	private val studyDayDataSource: StudyDayDao,
	//@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : StudyDayRepository {
	
	override fun getStudyDaysStream(): Flow<List<StudyDay>> {
		return studyDayDataSource.observeAll()
	}
	
	override fun getStudyDayStream(studyDayId: Long): Flow<StudyDay> {
		return studyDayDataSource.observeById(studyDayId)
	}
	
	override suspend fun getStudyDays(): List<StudyDay> {
		return studyDayDataSource.getAll()
	}
	
	override suspend fun getStudyDay(studyDayId: Long): StudyDay? {
		return studyDayDataSource.getById(studyDayId)
	}
	
	override suspend fun getStudyDayByDate(date: LocalDate): StudyDay? {
		return studyDayDataSource.getByDate(date)
	}
	
	override suspend fun getDayAndSchedulesWithSubjectsByDate(date: LocalDate): StudyDayWithSchedulesAndSubjects? {
		return studyDayDataSource.getByDateWithSchedulesAndSubjects(date)
	}
	
	override suspend fun create(studyDay: StudyDay): Long {
		return studyDayDataSource.upsert(studyDay)
	}
	
	override suspend fun update(studyDay: StudyDay) {
		studyDayDataSource.upsert(studyDay)
	}
	
	override suspend fun deleteAllStudyDays() {
		studyDayDataSource.deleteAll()
	}
	
	override suspend fun deleteStudyDay(studyDayId: Long) {
		studyDayDataSource.deleteById(studyDayId)
	}
	
}