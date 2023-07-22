package com.kxsv.schooldiary.data.local.features.study_day

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.STUDY_DAY_TABLE_NAME
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StudyDayDao {
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME")
	fun observeAll(): Flow<List<StudyDayEntity>>
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE studyDayId = :studyDayId")
	fun observeById(studyDayId: Long): Flow<StudyDayEntity>
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME")
	suspend fun getAll(): List<StudyDayEntity>
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE studyDayId = :studyDayId")
	suspend fun getById(studyDayId: Long): StudyDayEntity?
	
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE date = :date")
	suspend fun getByDate(date: LocalDate): StudyDayEntity?
	
	@Transaction
	@Query("SELECT * FROM $STUDY_DAY_TABLE_NAME WHERE date = :date")
	suspend fun getByDateWithSchedulesAndSubjects(date: LocalDate): StudyDayWithSchedulesAndSubjects?
	
	@Upsert
	suspend fun upsertAll(studyDays: List<StudyDayEntity>)
	
	@Upsert
	suspend fun upsert(studyDay: StudyDayEntity): Long
	
	@Query("DELETE FROM $STUDY_DAY_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $STUDY_DAY_TABLE_NAME WHERE studyDayId = :studyDayId")
	suspend fun deleteById(studyDayId: Long)
}