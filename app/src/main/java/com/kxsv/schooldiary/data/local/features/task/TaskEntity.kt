package com.kxsv.schooldiary.data.local.features.task

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import java.time.LocalDate

@Entity(
	tableName = DatabaseConstants.TASK_TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = SubjectEntity::class,
			parentColumns = ["subjectId"],
			childColumns = ["subjectMasterId"],
			onDelete = ForeignKey.SET_DEFAULT
		),
	],
	indices = [Index(value = ["subjectMasterId"])]
)
data class TaskEntity(
	val title: String,
	val description: String = "",
	val dueDate: LocalDate,
	val subjectMasterId: String?,
	val isDone: Boolean = false,
	val isFetched: Boolean = false,
	@PrimaryKey
	val taskId: String = "",
)
