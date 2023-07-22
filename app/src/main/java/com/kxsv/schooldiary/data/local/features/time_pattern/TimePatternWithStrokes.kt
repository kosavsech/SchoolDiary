package com.kxsv.schooldiary.data.local.features.time_pattern

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity

data class TimePatternWithStrokes(
	@Embedded
	val timePattern: TimePatternEntity,
	@Relation(
		entity = PatternStrokeEntity::class,
		parentColumn = "patternId",
		entityColumn = "patternMasterId",
	)
	val strokes: List<PatternStrokeEntity>,
)