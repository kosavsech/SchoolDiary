package com.kxsv.schooldiary.data.local.features.subject

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.local.features.teacher.TeacherEntity

data class SubjectWithTeachers(
	@Embedded
	val subject: SubjectEntity,
	@Relation(
		entity = TeacherEntity::class,
		parentColumn = "subjectId",
		entityColumn = "teacherId",
		associateBy = Junction(SubjectTeacher::class)
	)
	val teachers: Set<TeacherEntity>,
)