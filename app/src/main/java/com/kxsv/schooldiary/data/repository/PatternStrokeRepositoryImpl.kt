package com.kxsv.schooldiary.data.repository

import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeDao
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternStrokeRepositoryImpl @Inject constructor(
    private val localDataSource: PatternStrokeDao
) : PatternStrokeRepository {
	
	override fun getStrokesStream(): Flow<List<PatternStrokeEntity>> {
		return localDataSource.observeAll()
	}
	
	override fun getStrokesStreamByPatternId(patternId: Long): Flow<List<PatternStrokeEntity>> {
		return localDataSource.observeAllByPatternId(patternId)
	}
	
	override fun getStrokeStream(id: Int): Flow<PatternStrokeEntity> {
		return localDataSource.observeById(id)
	}
	
	override suspend fun getStrokes(): List<PatternStrokeEntity> {
		return localDataSource.getAll()
	}
	
	override suspend fun getStrokesByPatternId(patternId: Long): List<PatternStrokeEntity> {
		return localDataSource.getAllByPatternId(patternId)
	}
	
	override suspend fun getStroke(id: Int): PatternStrokeEntity? {
		return localDataSource.getById(id)
	}
	
	override suspend fun createStrokes(patternStrokes: List<PatternStrokeEntity>) {
		return localDataSource.upsertAll(patternStrokes)
	}
	
	override suspend fun createStroke(patternStroke: PatternStrokeEntity) {
		return localDataSource.upsert(patternStroke)
	}
	
	override suspend fun updateStrokes(patternStrokes: List<PatternStrokeEntity>) {
		return localDataSource.upsertAll(patternStrokes)
	}
	
	override suspend fun updateStroke(patternStroke: PatternStrokeEntity) {
		return localDataSource.upsert(patternStroke)
	}

    override suspend fun deleteAll() {
        return localDataSource.deleteAll()
    }
    
    override suspend fun deleteStrokeById(id: Int) {
        return localDataSource.deleteById(id)
    }

}