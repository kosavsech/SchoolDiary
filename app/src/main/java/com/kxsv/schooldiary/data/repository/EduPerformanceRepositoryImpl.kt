package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceDao
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.dtos.EduPerformanceDto
import com.kxsv.schooldiary.data.remote.parsers.EduPerformanceParser
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.ApplicationScope
import com.kxsv.schooldiary.di.util.Dispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "EduPerformanceRepositor"

@Singleton
class EduPerformanceRepositoryImpl @Inject constructor(
	private val eduPerformanceDataSource: EduPerformanceDao,
	private val userPreferencesRepository: UserPreferencesRepository,
	private val webService: WebService,
	private val subjectDataSource: SubjectDao,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
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
	 * @throws java.net.MalformedURLException
	 * @throws org.jsoup.HttpStatusException
	 * @throws org.jsoup.UnsupportedMimeTypeException
	 * @throws java.net.SocketTimeoutException
	 * @throws java.io.IOException
	 */
	override suspend fun fetchEduPerformanceByTerm(term: EduPerformancePeriod): List<EduPerformanceDto> {
		val rows = webService.getTermEduPerformance(term)
		return if (term != EduPerformancePeriod.YEAR) {
			EduPerformanceParser().parseTerm(rows, term)
		} else {
			EduPerformanceParser().parseYear(rows)
		}
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 * @throws java.net.MalformedURLException
	 * @throws org.jsoup.HttpStatusException
	 * @throws org.jsoup.UnsupportedMimeTypeException
	 * @throws java.net.SocketTimeoutException
	 * @throws java.io.IOException
	 */
	override suspend fun fetchEduPerformance(): List<List<EduPerformanceDto>> = coroutineScope {
		val periodType = userPreferencesRepository.getEducationPeriodType()
		val termIndexRange = if (periodType == PeriodType.TERMS) {
			0..3
		} else {
			0..1
		}.toMutableList()
		termIndexRange.add(4)
		val performanceEntities = mutableListOf<Deferred<List<EduPerformanceDto>>>()
		for (termIndex in termIndexRange) {
			val performanceEntity = async {
				val term = EduPerformancePeriod.values()[termIndex]
				fetchEduPerformanceByTerm(term = term)
			}
			performanceEntities.add(performanceEntity)
		}
		return@coroutineScope performanceEntities.awaitAll()
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
	
	override suspend fun upsertAll(eduPerformances: List<EduPerformanceEntity>) {
		eduPerformanceDataSource.upsertAll(eduPerformances)
	}
	
	override suspend fun deleteAllEduPerformances() {
		eduPerformanceDataSource.deleteAll()
	}
	
	override suspend fun deleteEduPerformance(eduPerformanceId: String) {
		eduPerformanceDataSource.deleteById(eduPerformanceId)
	}
	
}