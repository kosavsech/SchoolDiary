package com.kxsv.schooldiary.data.mapper

import com.kxsv.schooldiary.data.local.features.task.TaskEntity
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.remote.dtos.TaskDto
import com.kxsv.schooldiary.data.util.DataIdGenUtils.generateTaskId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun TaskDto.toTaskEntity(): TaskEntity {
	return TaskEntity(
		title = title,
		description = "Fetched from edu.tatar on ${
			LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
		}",
		dueDate = dueDate,
		subjectMasterId = subject.subjectId,
		isFetched = true,
		taskId = generateTaskId(dueDate, subject.subjectId, lessonIndex)
	)
}

fun List<TaskDto>.toTaskEntities() = map { it.toTaskEntity() }

fun TaskDto.toTaskWithSubject(): TaskWithSubject {
	return TaskWithSubject(
		TaskEntity(
			title = title,
			description = "Fetched from edu.tatar on ${
				LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
			}",
			dueDate = dueDate,
			subjectMasterId = subject.subjectId,
			isFetched = true,
			taskId = generateTaskId(dueDate, subject.subjectId, lessonIndex)
		),
		subject = subject,
	)
}


fun List<TaskDto>.toTasksWithSubject() = map { it.toTaskWithSubject() }

