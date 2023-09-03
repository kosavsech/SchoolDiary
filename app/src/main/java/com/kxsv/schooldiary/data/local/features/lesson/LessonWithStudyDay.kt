package com.kxsv.schooldiary.data.local.features.lesson

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity

data class LessonWithStudyDay(
	@Embedded
	val lesson: LessonEntity,
	@Relation(
		entity = StudyDayEntity::class,
		parentColumn = "studyDayMasterId",
		entityColumn = "studyDayId",
	)
	val studyDay: StudyDayEntity,
)