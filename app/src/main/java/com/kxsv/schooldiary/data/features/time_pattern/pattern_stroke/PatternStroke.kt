package com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.features.time_pattern.TimePattern
import java.time.LocalTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TimePattern::class,
            parentColumns = ["patternId"],
            childColumns = ["patternMasterId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class PatternStroke(
	@ColumnInfo(index = true)
	var patternMasterId: Long? = null,
	// TODO: add indexes
	// TODO: everywhere when we create this times set seconds and etc to zero
	val startTime: LocalTime,
	val endTime: LocalTime,
	@PrimaryKey(autoGenerate = true)
	val strokeId: Int = 0,
)