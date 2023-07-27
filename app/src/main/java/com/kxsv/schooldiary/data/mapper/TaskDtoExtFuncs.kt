package com.kxsv.schooldiary.data.mapper

import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.remote.task.TaskDto

fun TaskDto.toTaskEntity(): TaskEntity {
	try {
		return TaskEntity(
			title = title,
			dueDate = dueDate,
			description = "",
			subjectMasterId = subjectId
		)
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert network to local", e)
	}
}

fun List<TaskDto>.toTaskEntities() = map { it.toTaskEntity() }