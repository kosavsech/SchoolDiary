package com.kxsv.schooldiary.data.local.features.edu_performance

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.kxsv.schooldiary.data.local.features.DatabaseConstants
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod

@Entity(
	tableName = DatabaseConstants.EDU_PERFORMANCE_TABLE_NAME,
	foreignKeys = [
		ForeignKey(
			entity = SubjectEntity::class,
			parentColumns = ["subjectId"],
			childColumns = ["subjectMasterId"],
		),
	],
	indices = [Index(value = ["subjectMasterId"])]
)
data class EduPerformanceEntity(
	val subjectMasterId: Long? = null,
	val marks: List<Mark?>,
	val finalMark: Mark?,
	val examMark: Mark?,
	val period: EduPerformancePeriod,
	@PrimaryKey
	val eduPerformanceId: String,
)
