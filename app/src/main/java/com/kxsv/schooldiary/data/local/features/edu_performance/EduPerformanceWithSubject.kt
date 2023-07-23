package com.kxsv.schooldiary.data.local.features.edu_performance

import androidx.room.Embedded
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity

data class EduPerformanceWithSubject(
	@Embedded
	val eduPerformance: EduPerformanceEntity,
	@Relation(
		entity = SubjectEntity::class,
		parentColumn = "subjectMasterId",
		entityColumn = "subjectId",
	)
	val subject: SubjectEntity,
)
