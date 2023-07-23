package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceDao
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectDao
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.edu_performance.EduPerformanceDto
import com.kxsv.schooldiary.data.remote.edu_performance.EduPerformanceParser
import com.kxsv.schooldiary.di.ApplicationScope
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
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
	
	override suspend fun observeAllWithSubjectForPeriod(period: EduPerformancePeriod): Flow<List<EduPerformanceWithSubject>> {
		return eduPerformanceDataSource.observeAllWithSubjectByPeriod(period)
	}
	
	override suspend fun getEduPerformances(): List<EduPerformanceEntity> {
		return eduPerformanceDataSource.getAll()
	}
	
	override suspend fun fetchEduPerformanceByTerm(term: Int): List<EduPerformanceDto> {
		val rows = webService.getTermEduPerformance(term)
		return EduPerformanceParser().parseTerm(rows, term.toString())
	}
	
	override suspend fun fetchEduPerformance(): Unit = withContext(ioDispatcher) {
		for (i in 1..4) {
			async {
				val performanceEntities = fetchEduPerformanceByTerm(i).toEduPerformanceEntities()
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
	
	private suspend fun EduPerformanceDto.toEduPerformanceEntity(): EduPerformanceEntity {
		try {
			val subjectMasterId =
				subjectDataSource.getByName(subjectAncestorName)?.subjectId
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			// TODO: add prompt to create subject with such name, or create it forcibly
			return EduPerformanceEntity(
				subjectMasterId = subjectMasterId,
				marks = marks,
				finalMark = finalMark,
				period = period,
				eduPerformanceId = generateEduPerformanceId(subjectAncestorName, period)
			)
			
		} catch (e: NoSuchElementException) {
			throw RuntimeException("Failed to convert network to local", e)
		}
	}
	
	private suspend fun List<EduPerformanceDto>.toEduPerformanceEntities() =
		map { it.toEduPerformanceEntity() }
	
	
	private fun generateEduPerformanceId(
		subjectAncestorName: String,
		period: EduPerformancePeriod,
	): String {
		return (subjectAncestorName + "_" + period.toString())
	}
	
}