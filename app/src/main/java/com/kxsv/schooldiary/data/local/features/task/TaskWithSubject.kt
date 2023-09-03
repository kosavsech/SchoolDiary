package com.kxsv.schooldiary.data.local.features.task

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity

data class TaskWithSubject(
	@Embedded
	val taskEntity: TaskEntity,
	@Relation(
		entity = SubjectEntity::class,
		parentColumn = "subjectMasterId",
		entityColumn = "subjectId",
	)
	val subject: SubjectEntity,
)