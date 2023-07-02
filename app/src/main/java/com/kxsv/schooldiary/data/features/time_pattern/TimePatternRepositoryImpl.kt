package com.kxsv.schooldiary.data.features.time_pattern

import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStrokeDao
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
        strokes.forEach { stroke ->
            stroke.patternMasterId = patternId
        }
        strokesDataSource.upsertAll(strokes)
        return patternId
    }

    override suspend fun updatePatternWithStrokes(
        patternId: Long,
        name: String,
        strokes: List<PatternStroke>,
    ) {
        val pattern = patternDataSource.getById(patternId)?.copy(name = name)
            ?: throw NoSuchElementException("Pattern(scheduleId $patternId) not found")

        patternDataSource.upsert(pattern)
        strokes.forEach { stroke ->
            stroke.patternMasterId = patternId
        }
        strokesDataSource.upsertAll(strokes)
    }

    override suspend fun deleteAllPatterns() {
        patternDataSource.deleteAll()
    }

    override suspend fun deletePattern(patternId: Long) {
        patternDataSource.deleteById(patternId)
    }
}