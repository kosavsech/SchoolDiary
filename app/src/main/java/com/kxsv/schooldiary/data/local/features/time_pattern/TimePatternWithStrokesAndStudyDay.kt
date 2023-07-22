package com.kxsv.schooldiary.data.local.features.time_pattern

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity

data class TimePatternWithStrokesAndStudyDay(
	@Embedded
	val patternWithStrokes: TimePatternWithStrokes,
	@Relation(
		entity = StudyDayEntity::class,
		parentColumn = "patternId",
		entityColumn = "appliedPatternId"
	)
	val studyDay: StudyDayEntity,
)