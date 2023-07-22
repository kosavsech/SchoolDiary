package com.kxsv.schooldiary.data.local.features.subject

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity

data class SubjectWithSchedules(
	@Embedded
	val subject: SubjectEntity,
	@Relation(
		entity = LessonEntity::class,
		parentColumn = "subjectId",
		entityColumn = "subjectAncestorId",
	)
	val lessons: List<LessonEntity>,
)