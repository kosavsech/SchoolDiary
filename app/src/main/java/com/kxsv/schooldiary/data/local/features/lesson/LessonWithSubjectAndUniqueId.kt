package com.kxsv.schooldiary.data.local.features.lesson

import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.task.TaskEntity

data class LessonWithSubjectAndUniqueId(
	val taskEntity: TaskEntity,
	val subject: SubjectEntity,
	val uniqueId: String,
)