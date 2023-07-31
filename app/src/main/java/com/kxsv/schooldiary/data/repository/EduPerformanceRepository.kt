package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.data.remote.edu_performance.EduPerformanceDto
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
import kotlinx.coroutines.flow.Flow

interface EduPerformanceRepository {
    
    fun observeAll(): Flow<List<EduPerformanceEntity>>
    
    fun observeAllWithSubject(): Flow<List<EduPerformanceWithSubject>>
    
    fun observeEduPerformance(eduPerformanceId: String): Flow<EduPerformanceEntity>
    
    fun observeEduPerformanceBySubject(
	    subjectId: Long,
	    period: EduPerformancePeriod,
    ): Flow<EduPerformanceEntity>
	
	fun observeAllWithSubjectForPeriod(period: EduPerformancePeriod): Flow<List<EduPerformanceWithSubject>>
	
	suspend fun getEduPerformances(): List<EduPerformanceEntity>
	
	suspend fun fetchEduPerformanceByTerm(term: String): List<EduPerformanceDto>
	
	/**
	 * @throws RuntimeException
	 */
	suspend fun fetchEduPerformance()
	
	suspend fun getEduPerformance(eduPerformanceId: String): EduPerformanceEntity?
	
	suspend fun createEduPerformance(eduPerformance: EduPerformanceEntity)
	
	suspend fun updateEduPerformance(eduPerformance: EduPerformanceEntity)
	
	suspend fun deleteAllEduPerformances()
	
	suspend fun deleteEduPerformance(eduPerformanceId: String)
}