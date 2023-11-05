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
	
	override fun observeWeekSampleNext(currentDate: LocalDate): Flow<List<StudyDayWithSchedulesAndSubjects>> {
		return studyDayDataSource.observeWeekSample(
			startDate = currentDate,
			currentDate.plusDays(6)
		)
	}
	
	override fun observeWeekSampleBefore(currentDate: LocalDate): Flow<List<StudyDayWithSchedulesAndSubjects>> {
		return studyDayDataSource.observeWeekSample(
			startDate = currentDate.minusDays(6),
			currentDate
		)
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
	
	override suspend fun getWeekSample(currentDate: LocalDate): List<StudyDayWithSchedulesAndSubjects>? {
		val var1 =
			studyDayDataSource.getWeekSample(startDate = currentDate, currentDate.plusDays(6))
		val var2 =
			studyDayDataSource.getWeekSample(startDate = currentDate.minusDays(6), currentDate)
		return if (var1 != null) {
			if (var2 != null) {
				if (var2.size <= var1.size) var1 else var2
			} else {
				var1
			}
		} else var2
	}
	
	override suspend fun getDateForNextLessonOfSubject(
		subjectId: String,
		startDate: LocalDate,
	): LocalDate? {
		return studyDayDataSource.getDateForNextLessonOfSubject(subjectId, startDate)
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