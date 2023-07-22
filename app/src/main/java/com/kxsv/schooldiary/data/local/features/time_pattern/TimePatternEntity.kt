package com.kxsv.schooldiary.data.local.features.time_pattern

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants

@Entity(
	tableName = DatabaseConstants.TIME_PATTERN_TABLE_NAME
)
data class TimePatternEntity(
	val name: String,
	@PrimaryKey(autoGenerate = true)
	val patternId: Long = 0,
)