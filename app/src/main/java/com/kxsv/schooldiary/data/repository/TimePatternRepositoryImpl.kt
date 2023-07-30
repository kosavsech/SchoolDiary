package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternDao
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternWithStrokes
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimePatternRepositoryImpl @Inject constructor(
	private val patternDataSource: TimePatternDao,
	private val strokesDataSource: PatternStrokeDao,
	//@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : TimePatternRepository {
	
	override fun getPatternsStream(): Flow<List<TimePatternEntity>> {
		return patternDataSource.observeAll()
	}
	
	override fun observePatternsWithStrokes(): Flow<List<TimePatternWithStrokes>> {
		return patternDataSource.observeAllWithStrokes()
	}
	
	override suspend fun getPatterns(): List<TimePatternEntity> {
		return patternDataSource.getAll()
	}
	
	override fun getPatternWithStrokesStream(patternId: Long): Flow<TimePatternWithStrokes?> {
		return patternDataSource.observeByIdWithStrokes(patternId)
	}
	
	override suspend fun getPatternWithStrokes(patternId: Long): TimePatternWithStrokes? {
		return patternDataSource.getByIdWithStrokes(patternId)
	}
	
	override suspend fun getPattern(patternId: Long): TimePatternEntity? {
		return patternDataSource.getById(patternId)
	}
	
	override suspend fun createPattern(pattern: TimePatternEntity): Long {
		return patternDataSource.upsert(pattern)
	}
	
	override suspend fun createPatternWithStrokes(
		name: String,
		strokes: List<PatternStrokeEntity>,
	): Long {
		val patternId = createPattern(TimePatternEntity(name = name))
		val newStrokes: MutableList<PatternStrokeEntity> = mutableListOf()
		strokes.forEach { stroke ->
			newStrokes.add(stroke.copy(patternMasterId = patternId))
		}
		strokesDataSource.upsertAll(newStrokes)
		return patternId
	}
	
	override suspend fun updatePatternWithStrokes(
		pattern: TimePatternEntity,
		strokes: List<PatternStrokeEntity>,
	) {
		patternDataSource.upsert(pattern)
		val newStrokes: MutableList<PatternStrokeEntity> = mutableListOf()
		strokes.forEach { stroke ->
			newStrokes.add(stroke.copy(patternMasterId = pattern.patternId))
		}
		strokesDataSource.upsertAll(newStrokes)
	}
	
	override suspend fun deleteAllPatterns() {
		patternDataSource.deleteAll()
	}
	
	override suspend fun deletePattern(patternId: Long) {
		patternDataSource.deleteById(patternId)
	}
}