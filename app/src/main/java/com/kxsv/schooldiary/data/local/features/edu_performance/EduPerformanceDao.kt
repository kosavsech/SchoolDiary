package com.kxsv.schooldiary.data.local.features.edu_performance

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
import kotlinx.coroutines.flow.Flow

@Dao
interface EduPerformanceDao {
	
	@Query("SELECT * FROM ${DatabaseConstants.EDU_PERFORMANCE_TABLE_NAME}")
	fun observeAll(): Flow<List<EduPerformanceEntity>>
	
	@Transaction
	@Query("SELECT * FROM ${DatabaseConstants.EDU_PERFORMANCE_TABLE_NAME}")
	fun observeAllWithSubject(): Flow<List<EduPerformanceWithSubject>>
	
	@Transaction
	@Query("SELECT * FROM ${DatabaseConstants.EDU_PERFORMANCE_TABLE_NAME} WHERE period = :period")
	fun observeAllWithSubjectByPeriod(period: EduPerformancePeriod): Flow<List<EduPerformanceWithSubject>>
	
	@Query("SELECT * FROM ${DatabaseConstants.EDU_PERFORMANCE_TABLE_NAME} WHERE eduPerformanceId = :eduPerformanceId")
	fun observeById(eduPerformanceId: String): Flow<EduPerformanceEntity>
	
	@Query("SELECT * FROM ${DatabaseConstants.EDU_PERFORMANCE_TABLE_NAME}")
	suspend fun getAll(): List<EduPerformanceEntity>
	
	@Query("SELECT * FROM ${DatabaseConstants.EDU_PERFORMANCE_TABLE_NAME} WHERE eduPerformanceId = :eduPerformanceId")
	suspend fun getById(eduPerformanceId: String): EduPerformanceEntity?
	
	@Upsert
	suspend fun upsertAll(eduPerformances: List<EduPerformanceEntity>)
	
	@Upsert
	suspend fun upsert(eduPerformance: EduPerformanceEntity)
	
	@Query("DELETE FROM ${DatabaseConstants.EDU_PERFORMANCE_TABLE_NAME}")
	suspend fun deleteAll()
	
	@Query("DELETE FROM ${DatabaseConstants.EDU_PERFORMANCE_TABLE_NAME} WHERE eduPerformanceId = :eduPerformanceId")
	suspend fun deleteById(eduPerformanceId: String)
}