package com.kxsv.schooldiary.data.local.features.grade

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.subject.Subject
import com.kxsv.schooldiary.util.Mark
import java.time.LocalDate

@Entity(
	foreignKeys = [
		ForeignKey(
			entity = Subject::class,
			parentColumns = ["subjectId"],
			childColumns = ["subjectMasterId"],
		),
	],
	indices = [
		Index(
			value = ["mark", "typeOfWork", "date", "subjectMasterId", "index", "lessonIndex"],
			unique = true
		)
	]
)
data class Grade(
	val mark: Mark,
	val typeOfWork: String,
	val date: LocalDate,
	val subjectMasterId: Long,
	val lessonIndex: Int = 0,
	val index: Int = 0,
	@PrimaryKey(autoGenerate = true)
	val gradeId: Long = 0,
)

data class GradeWithSubject(
	@Embedded
	val grade: Grade,
	@Relation(
		entity = Subject::class,
		parentColumn = "subjectMasterId",
		entityColumn = "subjectId",
	)
	val subject: Subject,
)