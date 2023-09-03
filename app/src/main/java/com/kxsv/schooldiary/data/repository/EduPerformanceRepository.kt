package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.data.remote.dtos.EduPerformanceDto
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import kotlinx.coroutines.flow.Flow

interface EduPerformanceRepository {
	
	fun observeAll(): Flow<List<EduPerformanceEntity>>
	
	fun observeAllWithSubject(): Flow<List<EduPerformanceWithSubject>>
	
	fun observeEduPerformance(eduPerformanceId: String): Flow<EduPerformanceEntity>
	
	fun observeEduPerformanceBySubject(
		subjectId: String,
		period: EduPerformancePeriod,
	): Flow<EduPerformanceEntity>
	
	fun observeAllWithSubjectForPeriod(period: EduPerformancePeriod): Flow<List<EduPerformanceWithSubject>>
	
	suspend fun getEduPerformances(): List<EduPerformanceEntity>
	
	suspend fun fetchEduPerformanceByTerm(term: EduPerformancePeriod): List<EduPerformanceDto>
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 * @throws java.net.MalformedURLException
	 * @throws org.jsoup.HttpStatusException
	 * @throws org.jsoup.UnsupportedMimeTypeException
	 * @throws java.net.SocketTimeoutException
	 * @throws java.io.IOException
	 */
	suspend fun fetchEduPerformance()
	
	suspend fun getEduPerformance(eduPerformanceId: String): EduPerformanceEntity?
	
	suspend fun createEduPerformance(eduPerformance: EduPerformanceEntity)
	
	suspend fun updateEduPerformance(eduPerformance: EduPerformanceEntity)
	
	suspend fun deleteAllEduPerformances()
	
	suspend fun deleteEduPerformance(eduPerformanceId: String)
}