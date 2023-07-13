package com.kxsv.schooldiary.data.features.grade

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.util.Mark
import java.time.LocalDate

@Entity
data class Grade(
	val mark: Mark,
	val typeOfWork: String,
	val date: LocalDate,
	val subjectMasterId: Long,
	@PrimaryKey(autoGenerate = true)
	val gradeId: Long = 0,
)