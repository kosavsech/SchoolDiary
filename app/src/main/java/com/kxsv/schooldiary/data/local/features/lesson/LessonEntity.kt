package com.kxsv.schooldiary.data.local.features.lesson

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity

@Entity(
	tableName = DatabaseConstants.LESSON_TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = SubjectEntity::class,
			parentColumns = ["subjectId"],
			childColumns = ["subjectAncestorId"],
			onDelete = ForeignKey.SET_DEFAULT, // TODO: fix of behaviour so we first memorise name at least
			onUpdate = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = StudyDayEntity::class,
			parentColumns = ["studyDayId"],
			childColumns = ["studyDayMasterId"],
			onDelete = ForeignKey.NO_ACTION,
			onUpdate = ForeignKey.CASCADE
		),
	]
)
data class LessonEntity(
	val index: Int,
	// TODO: add manual start time / end time configuration
	@ColumnInfo(index = true)
	val studyDayMasterId: Long? = null,
	@ColumnInfo(index = true)
	val subjectAncestorId: Long? = null,
	@PrimaryKey(autoGenerate = true)
	val lessonId: Long = 0,
)