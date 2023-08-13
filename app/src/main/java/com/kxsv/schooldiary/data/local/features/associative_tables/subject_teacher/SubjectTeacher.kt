package com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity

@Entity(
	primaryKeys = ["subjectId", "teacherId"],
	indices = [Index(value = ["teacherId"])],
	foreignKeys = [
		ForeignKey(
			entity = SubjectEntity::class,
			parentColumns = ["subjectId"],
			childColumns = ["subjectId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = TeacherEntity::class,
			parentColumns = ["teacherId"],
			childColumns = ["teacherId"],
			onDelete = ForeignKey.CASCADE
		),
	],
)
data class SubjectTeacher(
	val subjectId: String,
	val teacherId: String,
)