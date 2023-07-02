package com.kxsv.schooldiary.data.features.schedule

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM Schedule")
    fun observeAll(): Flow<List<Schedule>>

    @Transaction
    @Query("SELECT * FROM Schedule WHERE date = :date")
    fun observeAllWithSubjectByDate(date: LocalDate):  Flow<List<ScheduleWithSubject>>

    @Query("SELECT * FROM Schedule WHERE scheduleId = :scheduleId")
    fun observeById(scheduleId: Long): Flow<Schedule>

    /*    @Transaction
        @Query("SELECT * FROM Schedule WHERE scheduleId = :scheduleId")
        suspend fun observeByIdWithSubject(scheduleId: Int): ScheduleWithSubject?*/

    @Query("SELECT * FROM Schedule")
    suspend fun getAll(): List<Schedule>

    @Query("SELECT * FROM Schedule WHERE scheduleId = :scheduleId")
    suspend fun getById(scheduleId: Long): Schedule?

    @Transaction
    @Query("SELECT * FROM Schedule WHERE scheduleId = :scheduleId")
    suspend fun getByIdWithSubject(scheduleId: Long): ScheduleWithSubject?

    @Upsert
    suspend fun upsertAll(schedules: List<Schedule>)

    @Upsert
    suspend fun upsert(schedule: Schedule)

    @Query("DELETE FROM Schedule")
    suspend fun deleteAll()

    @Query("DELETE FROM Schedule WHERE scheduleId = :scheduleId")
    suspend fun deleteById(scheduleId: Long)
}