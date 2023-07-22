package com.kxsv.schooldiary.data.local.features.teacher

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity

data class TeacherWithSubjects(
	@Embedded
	val teacher: TeacherEntity,
	@Relation(
		entity = SubjectEntity::class,
		parentColumn = "teacherId",
		entityColumn = "subjectId",
		associateBy = Junction(SubjectTeacher::class)
	)
	val subjects: List<SubjectEntity>,
)