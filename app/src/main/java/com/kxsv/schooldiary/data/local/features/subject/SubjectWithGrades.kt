package com.kxsv.schooldiary.data.local.features.subject

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity

data class SubjectWithGrades(
	@Embedded
	val subject: SubjectEntity,
	@Relation(
		entity = GradeEntity::class,
		parentColumn = "subjectId",
		entityColumn = "subjectMasterId",
	)
	val schedules: List<GradeEntity>,
)