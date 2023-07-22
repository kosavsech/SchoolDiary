package com.kxsv.schooldiary.data.local.features.teacher

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants

@Entity(
	tableName = DatabaseConstants.TEACHER_TABLE_NAME
)
data class TeacherEntity(
	val firstName: String,
	val lastName: String,
	val patronymic: String,
	val phoneNumber: String,
	@PrimaryKey(autoGenerate = true)
	val teacherId: Int = 0,
)