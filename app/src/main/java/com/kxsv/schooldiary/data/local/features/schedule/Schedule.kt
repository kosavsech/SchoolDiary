package com.kxsv.schooldiary.data.local.features.schedule

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.study_day.StudyDay
import com.kxsv.schooldiary.data.local.features.subject.Subject

@Entity(
	foreignKeys = [
		ForeignKey(
			entity = Subject::class,
			parentColumns = ["subjectId"],
			childColumns = ["subjectAncestorId"],
			onDelete = ForeignKey.NO_ACTION, // TODO: fix of behaviour so we first memorise name at least
			onUpdate = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = StudyDay::class,
			parentColumns = ["studyDayId"],
			childColumns = ["studyDayMasterId"],
			onDelete = ForeignKey.NO_ACTION,
			onUpdate = ForeignKey.CASCADE
		),
	]
)

data class Schedule(
	val index: Int,
	// TODO: add manual start time / end time configuration
	@ColumnInfo(index = true)
	val studyDayMasterId: Long? = null,
	@ColumnInfo(index = true)
	val subjectAncestorId: Long? = null,
	@PrimaryKey(autoGenerate = true)
	val scheduleId: Long = 0,
)

data class ScheduleWithSubject(
	@Embedded
	val schedule: Schedule,
	@Relation(
		entity = Subject::class,
		parentColumn = "subjectAncestorId",
		entityColumn = "subjectId",
	)
	val subject: Subject
)

data class ScheduleWithStudyDay(
	@Embedded
	val schedule: Schedule,
	@Relation(
		entity = StudyDay::class,
		parentColumn = "studyDayMasterId",
		entityColumn = "studyDayId",
	)
	val studyDay: StudyDay
)
