package com.kxsv.schooldiary.data.local.features.study_day

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.LESSON_TABLE_NAME
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.STUDY_DAY_TABLE_NAME
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StudyDayDao {
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME")
	fun observeAll(): Flow<List<StudyDayEntity>>
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE studyDayId = :studyDayId")
	fun observeById(studyDayId: Long): Flow<StudyDayEntity>
	
	@Transaction
	@Query(
		"SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE date >= :startDate AND date <= :endDate " +
				"ORDER BY $STUDY_DAY_TABLE_NAME.date ASC " +
				"LIMIT 6"
	)
	fun observeWeekSample(
		startDate: LocalDate,
		endDate: LocalDate,
	): Flow<List<StudyDayWithSchedulesAndSubjects>>
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME")
	suspend fun getAll(): List<StudyDayEntity>
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE studyDayId = :studyDayId")
	suspend fun getById(studyDayId: Long): StudyDayEntity?
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE date = :date")
	suspend fun getByDate(date: LocalDate): StudyDayEntity?
	
	@Transaction
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE date = :date")
	suspend fun getByDateWithSchedulesAndSubjects(date: LocalDate): StudyDayWithSchedulesAndSubjects?
	
	@Transaction
	@Query(
		"SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE date >= :startDate AND date <= :endDate " +
				"ORDER BY $STUDY_DAY_TABLE_NAME.date ASC " +
				"LIMIT 6"
	)
	suspend fun getWeekSample(
		startDate: LocalDate,
		endDate: LocalDate,
	): List<StudyDayWithSchedulesAndSubjects>?
	
	@Transaction
	@RewriteQueriesToDropUnusedColumns
	@Query(
		"SELECT $STUDY_DAY_TABLE_NAME.date FROM $STUDY_DAY_TABLE_NAME " +
				"JOIN $LESSON_TABLE_NAME ON $STUDY_DAY_TABLE_NAME.studyDayId = $LESSON_TABLE_NAME.studyDayMasterId " +
				"WHERE $LESSON_TABLE_NAME.subjectAncestorId == :subjectId AND " +
				"$STUDY_DAY_TABLE_NAME.date > :startDate " +
				"ORDER BY $STUDY_DAY_TABLE_NAME.date ASC " +
				"LIMIT 1"
	)
	suspend fun getDateForNextLessonOfSubject(subjectId: String, startDate: LocalDate): LocalDate?
	
	@Upsert
	suspend fun upsertAll(studyDays: List<StudyDayEntity>)
	
	@Upsert
	suspend fun upsert(studyDay: StudyDayEntity): Long
	
	@Query("DELETE FROM $STUDY_DAY_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $STUDY_DAY_TABLE_NAME WHERE studyDayId = :studyDayId")
	suspend fun deleteById(studyDayId: Long)
}