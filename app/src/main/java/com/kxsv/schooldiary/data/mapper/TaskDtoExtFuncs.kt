package com.kxsv.schooldiary.data.mapper

import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.remote.task.TaskDto
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun TaskDto.toTaskEntity(): TaskEntity {
	try {
		return TaskEntity(
			title = title,
			dueDate = dueDate,
			description = "Fetched from edu.tatar on ${
				LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
			}",
			subjectMasterId = subjectId,
			isFetched = true
		)
	} catch (e: NoSuchElementException) {
		throw RuntimeException("Failed to convert network to local", e)
	}
}

fun List<TaskDto>.toTaskEntities() = map { it.toTaskEntity() }