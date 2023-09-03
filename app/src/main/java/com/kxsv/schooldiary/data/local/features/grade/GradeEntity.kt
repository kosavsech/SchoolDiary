package com.kxsv.schooldiary.data.local.features.grade

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.util.Mark
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(
	tableName = DatabaseConstants.GRADE_TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = SubjectEntity::class,
			parentColumns = ["subjectId"],
			childColumns = ["subjectMasterId"],
			onDelete = ForeignKey.RESTRICT
		),
	],
	indices = [Index(value = ["subjectMasterId"])]
)
data class GradeEntity(
	val mark: Mark,
	val typeOfWork: String,
	val date: LocalDate,
	val fetchDateTime: LocalDateTime,
	val subjectMasterId: String,
	val lessonIndex: Int = 0,
	val index: Int = 0,
	@PrimaryKey
	val gradeId: String = "",
)