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
		),
	],
	indices = [Index(value = ["subjectMasterId"])]
)
data class TaskEntity(
	val name: String,
	val description: String,
	val dueDate: LocalDate,
	val subjectMasterId: Long,
	@PrimaryKey(autoGenerate = true)
	val taskId: Long = 0,
)
