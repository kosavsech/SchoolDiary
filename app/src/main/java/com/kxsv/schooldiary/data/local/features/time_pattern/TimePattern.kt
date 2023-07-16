package com.kxsv.schooldiary.data.local.features.time_pattern

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.study_day.StudyDay
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStroke

@Entity
data class TimePattern(
    val name: String,
    @PrimaryKey(autoGenerate = true)
    val patternId: Long = 0,
)

data class PatternWithStrokes(
	@Embedded
	val timePattern: TimePattern,
	@Relation(
		entity = PatternStroke::class,
		parentColumn = "patternId",
		entityColumn = "patternMasterId",
	)
	val strokes: List<PatternStroke>,
)

data class PatternWithStrokesAndStudyDay(
	@Embedded
	val patternWithStrokes: PatternWithStrokes,
	@Relation(
		entity = StudyDay::class,
		parentColumn = "patternId",
		entityColumn = "appliedPatternId"
	)
	val studyDay: StudyDay,
)