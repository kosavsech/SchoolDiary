package com.kxsv.schooldiary.data.local.features.lesson

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity

data class LessonWithSubject(
	@Embedded
	val lesson: LessonEntity,
	@Relation(
		entity = SubjectEntity::class,
		parentColumn = "subjectAncestorId",
		entityColumn = "subjectId",
	)
	val subject: SubjectEntity,
)