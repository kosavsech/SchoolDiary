package com.kxsv.schooldiary.data.mapper

import com.kxsv.schooldiary.data.local.features.task.TaskAndUniqueIdWithSubject
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.remote.dtos.TaskDto
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateUniqueTaskId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun TaskDto.toTaskEntity(): TaskEntity {
	return TaskEntity(
		title = title,
		dueDate = dueDate,
		description = "Fetched from edu.tatar on ${
			LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
		}",
		subjectMasterId = subject.subjectId,
		isFetched = true
	)
}

fun List<TaskDto>.toTaskEntities() = map { it.toTaskEntity() }

fun TaskDto.toTaskAndUniqueIdWithSubject(): TaskAndUniqueIdWithSubject {
	return TaskAndUniqueIdWithSubject(
		TaskEntity(
			title = title,
			dueDate = dueDate,
			description = "Fetched from edu.tatar on ${
				LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
			}",
			subjectMasterId = subject.subjectId,
			isFetched = true
		),
		subject = subject,
		uniqueId = generateUniqueTaskId()
	)
}


fun List<TaskDto>.toTasksAndUniqueIdWithSubject() = map { it.toTaskAndUniqueIdWithSubject() }

