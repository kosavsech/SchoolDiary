package com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternEntity
import java.time.LocalTime

@Entity(
	tableName = DatabaseConstants.PATTERN_STROKE_TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = TimePatternEntity::class,
			parentColumns = ["patternId"],
			childColumns = ["patternMasterId"],
			onDelete = ForeignKey.CASCADE,
			onUpdate = ForeignKey.CASCADE
		)
	]
)
data class PatternStrokeEntity(
	val index: Int,
	@ColumnInfo(index = true)
	val patternMasterId: Long? = null,
	val startTime: LocalTime,
	val endTime: LocalTime,
	@PrimaryKey(autoGenerate = true)
	val strokeId: Int = 0,
)