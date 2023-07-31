package com.kxsv.schooldiary.data.mapper

import com.kxsv.schooldiary.data.local.features.task.TaskAndUniqueIdWithSubject
import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.remote.task.TaskDto
import java.time.LocalDateTime
import java.time.ZoneId
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

private fun TaskDto.generateUniqueTaskId(): String {
	val dateStamp: String =
		dueDate.atStartOfDay(ZoneId.of("Europe/Moscow")).toEpochSecond().toString()
	val subjectId: String = subject.subjectId.toString()
	val lessonIndex: String = lessonIndex.toString()
	
	return (dateStamp + "_" + subjectId + "_" + lessonIndex)
}

fun List<TaskDto>.toTasksAndUniqueIdWithSubject() = map { it.toTaskAndUniqueIdWithSubject() }

