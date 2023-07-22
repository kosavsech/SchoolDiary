package com.kxsv.schooldiary.data.local.features.grade

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity

data class GradeWithSubject(
	@Embedded
	val grade: GradeEntity,
	@Relation(
		entity = SubjectEntity::class,
		parentColumn = "subjectMasterId",
		entityColumn = "subjectId",
	)
	val subject: SubjectEntity,
)