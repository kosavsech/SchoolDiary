package com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke

import androidx.room.*
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.PATTERN_STROKE_TABLE_NAME
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.STUDY_DAY_TABLE_NAME
import com.kxsv.schooldiary.data.local.features.DatabaseConstants.TIME_PATTERN_TABLE_NAME
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PatternStrokeDao {
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME ORDER BY `index` ASC")
	fun observeAll(): Flow<List<PatternStrokeEntity>>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME WHERE patternMasterId = :patternId ORDER BY `index` ASC")
	fun observeAllByPatternId(patternId: Long): Flow<List<PatternStrokeEntity>>
	
	@MapInfo(keyColumn = "date")
	@Transaction
	@RewriteQueriesToDropUnusedColumns
	@Query(
		"SELECT * FROM $PATTERN_STROKE_TABLE_NAME " +
				"JOIN $TIME_PATTERN_TABLE_NAME ON $PATTERN_STROKE_TABLE_NAME.patternMasterId = $TIME_PATTERN_TABLE_NAME.patternId " +
				"JOIN $STUDY_DAY_TABLE_NAME ON $PATTERN_STROKE_TABLE_NAME.patternMasterId = $STUDY_DAY_TABLE_NAME.appliedPatternId " +
				"WHERE $STUDY_DAY_TABLE_NAME.date >= :startRange AND $STUDY_DAY_TABLE_NAME.date <= :endRange " +
				"ORDER BY $PATTERN_STROKE_TABLE_NAME.`index` ASC"
	)
	fun observeAllForDateRange(
		startRange: LocalDate,
		endRange: LocalDate,
	): Flow<Map<LocalDate, List<PatternStrokeEntity>>>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME WHERE strokeId = :id ORDER BY `index` ASC")
	fun observeById(id: Int): Flow<PatternStrokeEntity>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME ORDER BY startTime ASC")
	suspend fun getAll(): List<PatternStrokeEntity>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME WHERE patternMasterId = :patternId ORDER BY `index` ASC")
	suspend fun getAllByPatternId(patternId: Long): List<PatternStrokeEntity>
	
	@Query("SELECT * FROM $PATTERN_STROKE_TABLE_NAME WHERE strokeId = :id")
	suspend fun getById(id: Int): PatternStrokeEntity?
	
	@Upsert
	suspend fun upsertAll(strokes: List<PatternStrokeEntity>)
	
	@Upsert
	suspend fun upsert(stroke: PatternStrokeEntity)
	
	@Query("DELETE FROM $PATTERN_STROKE_TABLE_NAME")
	suspend fun deleteAll()
	
	@Query("DELETE FROM $PATTERN_STROKE_TABLE_NAME WHERE patternMasterId = :patternId")
	suspend fun deleteAllByPatternId(patternId: Long)
	
	@Query("DELETE FROM $PATTERN_STROKE_TABLE_NAME WHERE strokeId = :id")
	suspend fun deleteById(id: Int)
}