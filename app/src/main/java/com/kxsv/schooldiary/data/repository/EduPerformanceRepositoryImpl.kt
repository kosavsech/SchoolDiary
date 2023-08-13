package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceDao
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.mapper.toEduPerformanceEntities
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.dtos.EduPerformanceDto
import com.kxsv.schooldiary.data.remote.parsers.EduPerformanceParser
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.remote.NetworkException
import com.kxsv.schooldiary.di.util.ApplicationScope
import com.kxsv.schooldiary.di.util.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EduPerformanceRepositoryImpl @Inject constructor(
	private val eduPerformanceDataSource: EduPerformanceDao,
	private val webService: WebService,
	private val subjectDataSource: SubjectDao,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
	@ApplicationScope private val scope: CoroutineScope,
) : EduPerformanceRepository {
	
	override fun observeAll(): Flow<List<EduPerformanceEntity>> {
		return eduPerformanceDataSource.observeAll()
	}
	
	override fun observeAllWithSubject(): Flow<List<EduPerformanceWithSubject>> {
		return eduPerformanceDataSource.observeAllWithSubject()
	}
	
	override fun observeEduPerformance(eduPerformanceId: String): Flow<EduPerformanceEntity> {
		return eduPerformanceDataSource.observeById(eduPerformanceId)
	}
	
	override fun observeEduPerformanceBySubject(
		subjectId: String,
		period: EduPerformancePeriod,
	): Flow<EduPerformanceEntity> {
		return eduPerformanceDataSource.observeBySubjectId(subjectId, period)
	}
	
	override fun observeAllWithSubjectForPeriod(period: EduPerformancePeriod): Flow<List<EduPerformanceWithSubject>> {
		return eduPerformanceDataSource.observeAllWithSubjectByPeriod(period)
	}
	
	override suspend fun getEduPerformances(): List<EduPerformanceEntity> {
		return eduPerformanceDataSource.getAll()
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	override suspend fun fetchEduPerformanceByTerm(term: EduPerformancePeriod): List<EduPerformanceDto> {
		val rows = webService.getTermEduPerformance(term.value)
		return if (term != EduPerformancePeriod.YEAR) {
			EduPerformanceParser().parseTerm(rows, term)
		} else {
			EduPerformanceParser().parseYear(rows)
		}
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	override suspend fun fetchEduPerformance(): Unit = withContext(ioDispatcher) {
		for (termIndex in 0..4) {
			async {
				val term = EduPerformancePeriod.values()[termIndex]
				val performanceEntities = fetchEduPerformanceByTerm(term = term)
					.toEduPerformanceEntities(subjectDataSource)
				updateDatabase(performanceEntities)
			}
		}
	}
	
	override suspend fun getEduPerformance(eduPerformanceId: String): EduPerformanceEntity? {
		return eduPerformanceDataSource.getById(eduPerformanceId)
	}
	
	override suspend fun createEduPerformance(eduPerformance: EduPerformanceEntity) {
		return eduPerformanceDataSource.upsert(eduPerformance)
	}
	
	override suspend fun updateEduPerformance(eduPerformance: EduPerformanceEntity) {
		eduPerformanceDataSource.upsert(eduPerformance)
	}
	
	override suspend fun deleteAllEduPerformances() {
		eduPerformanceDataSource.deleteAll()
	}
	
	override suspend fun deleteEduPerformance(eduPerformanceId: String) {
		eduPerformanceDataSource.deleteById(eduPerformanceId)
	}
	
	private suspend fun updateDatabase(performanceEntities: List<EduPerformanceEntity>) =
		scope.launch {
			for (performanceEntity in performanceEntities) {
				eduPerformanceDataSource.upsert(performanceEntity)
			}
		}
	
}