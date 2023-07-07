package com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke

import com.kxsv.schooldiary.domain.PatternStrokeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternStrokeRepositoryImpl @Inject constructor(
    private val localDataSource: PatternStrokeDao
) : PatternStrokeRepository {

    override fun getStrokesStream(): Flow<List<PatternStroke>> {
        return localDataSource.observeAll()
    }

    override fun getStrokesStreamByPatternId(patternId: Long): Flow<List<PatternStroke>> {
        return localDataSource.observeAllByPatternId(patternId)
    }

    override fun getStrokeStream(id: Int): Flow<PatternStroke> {
        return localDataSource.observeById(id)
    }

    override suspend fun getStrokes(): List<PatternStroke> {
        return localDataSource.getAll()
    }

    override suspend fun getStrokesByPatternId(patternId: Long): List<PatternStroke> {
        return localDataSource.getAllByPatternId(patternId)
    }

    override suspend fun getStroke(id: Int): PatternStroke? {
        return localDataSource.getById(id)
    }

    override suspend fun createStrokes(patternStrokes: List<PatternStroke>) {
        return localDataSource.upsertAll(patternStrokes)
    }

    override suspend fun createStroke(patternStroke: PatternStroke) {
        return localDataSource.upsert(patternStroke)
    }

    override suspend fun updateStrokes(patternStrokes: List<PatternStroke>) {
        return localDataSource.upsertAll(patternStrokes)
    }

    override suspend fun updateStroke(patternStroke: PatternStroke) {
        return localDataSource.upsert(patternStroke)
    }

    override suspend fun deleteAll() {
        return localDataSource.deleteAll()
    }
    
    override suspend fun deleteStrokeById(id: Int) {
        return localDataSource.deleteById(id)
    }

}