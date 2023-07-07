package com.kxsv.schooldiary.data.features.study_day

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kxsv.schooldiary.data.features.schedule.Schedule
import com.kxsv.schooldiary.data.features.schedule.ScheduleWithSubject
import com.kxsv.schooldiary.data.features.time_pattern.TimePattern
import java.time.LocalDate

@Entity(
	foreignKeys = [
		ForeignKey(
			entity = TimePattern::class,
			parentColumns = ["patternId"],
			childColumns = ["appliedPatternId"],
			onDelete = ForeignKey.SET_DEFAULT,
			onUpdate = ForeignKey.CASCADE
		)
	]
)
data class StudyDay(
	val date: LocalDate,
	val appliedPatternId: Long? = null,
	@PrimaryKey(autoGenerate = true)
	val studyDayId: Long = 0,
)

data class StudyDayWithSchedulesAndSubjects(
	@Embedded
	val studyDay: StudyDay,
	@Relation(
		entity = Schedule::class,
		parentColumn = "studyDayId",
		entityColumn = "studyDayMasterId"
	)
	val classes: List<ScheduleWithSubject>
)