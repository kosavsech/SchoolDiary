package com.kxsv.schooldiary.data.features.time_pattern

import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.domain.TimePatternRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimePatternRepositoryImpl @Inject constructor(
	private val patternDataSource: TimePatternDao,
	private val strokesDataSource: PatternStrokeDao,
	//@DefaultDispatcher private val dispatcher: CoroutineDispatcher,
) : TimePatternRepository {
	
	override fun getPatternsStream(): Flow<List<TimePattern>> {
		return patternDataSource.observeAll()
	}
	
	override fun getPatternsWithStrokesStream(): Flow<List<PatternWithStrokes>> {
		return patternDataSource.observeAllWithStrokes()
	}
	
	override suspend fun getPatterns(): List<TimePattern> {
		return patternDataSource.getAll()
	}
	
	override fun getPatternWithStrokesStream(patternId: Long): Flow<PatternWithStrokes?> {
		return patternDataSource.observeByIdWithStrokes(patternId)
	}
	
	override suspend fun getPatternWithStrokes(patternId: Long): PatternWithStrokes? {
		return patternDataSource.getByIdWithStrokes(patternId)
	}
	
	override suspend fun createPattern(pattern: TimePattern): Long {
		return patternDataSource.upsert(pattern)
	}
	
	override suspend fun createPatternWithStrokes(
		name: String,
		strokes: List<PatternStroke>,
	): Long {
		val patternId = createPattern(TimePattern(name = name))
		val newStrokes: MutableList<PatternStroke> = mutableListOf()
		strokes.forEach { stroke ->
			newStrokes.add(stroke.copy(patternMasterId = patternId))
		}
		strokesDataSource.upsertAll(newStrokes)
		return patternId
	}
	
	override suspend fun updatePatternWithStrokes(
		pattern: TimePattern,
		strokes: List<PatternStroke>,
	) {
		patternDataSource.upsert(pattern)
		val newStrokes: MutableList<PatternStroke> = mutableListOf()
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