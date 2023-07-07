package com.kxsv.schooldiary.data.features.study_day

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface StudyDayDao {
	
	@Query("SELECT * FROM StudyDay")
	fun observeAll(): Flow<List<StudyDay>>
	
	@Query("SELECT * FROM StudyDay WHERE studyDayId = :studyDayId")
	fun observeById(studyDayId: Long): Flow<StudyDay>
	
	@Query("SELECT * FROM StudyDay")
	suspend fun getAll(): List<StudyDay>
	
	@Query("SELECT * FROM StudyDay WHERE studyDayId = :studyDayId")
	suspend fun getById(studyDayId: Long): StudyDay?
	
	@Query("SELECT * FROM StudyDay WHERE date = :date")
	suspend fun getByDate(date: LocalDate): StudyDay?
	
	@Transaction
	@Query("SELECT * FROM StudyDay WHERE date = :date")
	suspend fun getByDateWithSchedulesAndSubjects(date: LocalDate): StudyDayWithSchedulesAndSubjects?
	
	@Upsert
	suspend fun upsertAll(studyDays: List<StudyDay>)
	
	@Upsert
	suspend fun upsert(studyDay: StudyDay): Long
	
	@Query("DELETE FROM StudyDay")
	suspend fun deleteAll()
	
	@Query("DELETE FROM StudyDay WHERE studyDayId = :studyDayId")
	suspend fun deleteById(studyDayId: kotlin.Long)
}