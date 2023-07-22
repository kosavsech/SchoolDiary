package com.kxsv.schooldiary.data.local.features.study_day

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternEntity
import java.time.LocalDate

@Entity(
	tableName = DatabaseConstants.STUDY_DAY_TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = TimePatternEntity::class,
			parentColumns = ["patternId"],
			childColumns = ["appliedPatternId"],
			onDelete = ForeignKey.SET_DEFAULT,
			onUpdate = ForeignKey.CASCADE
		)
	],
	indices = [Index(value = ["appliedPatternId"])]
)
data class StudyDayEntity(
	val date: LocalDate,
	val appliedPatternId: Long? = null,
	@PrimaryKey(autoGenerate = true)
	val studyDayId: Long = 0,
)