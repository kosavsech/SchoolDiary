package com.kxsv.schooldiary.data.local.features.study_day

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject

data class StudyDayWithSchedulesAndSubjects(
	@Embedded
	val studyDay: StudyDayEntity,
	@Relation(
		entity = LessonEntity::class,
		parentColumn = "studyDayId",
		entityColumn = "studyDayMasterId"
	)
	val classes: List<LessonWithSubject>,
)