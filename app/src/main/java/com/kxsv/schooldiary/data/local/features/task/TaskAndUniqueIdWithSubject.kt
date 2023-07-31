package com.kxsv.schooldiary.data.local.features.task

import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity

data class TaskAndUniqueIdWithSubject(
	val taskEntity: TaskEntity,
	val subject: SubjectEntity,
	val uniqueId: String,
)
