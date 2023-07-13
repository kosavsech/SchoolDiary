package com.kxsv.schooldiary.data.features.subject

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.features.grade.Grade
import com.kxsv.schooldiary.data.features.schedule.Schedule
import com.kxsv.schooldiary.data.features.teacher.Teacher

@Entity
data class Subject(
    val name: String,
    val cabinet: String,
//    val tags: List<Tag>,
    @PrimaryKey(autoGenerate = true)
    val subjectId: Long = 0,
)

data class SubjectWithSchedules(
    @Embedded
    val subject: Subject,
    @Relation(
        entity = Schedule::class,
        parentColumn = "subjectId",
        entityColumn = "subjectAncestorId",
    )
    val schedules: List<Schedule>
)

data class SubjectWithTeachers(
    @Embedded
    val subject: Subject,
    @Relation(
        entity = Teacher::class,
        parentColumn = "subjectId",
        entityColumn = "teacherId",
        associateBy = Junction(SubjectTeacher::class)
    )
    val teachers: Set<Teacher>,
)

data class SubjectWithGrades(
	@Embedded
	val subject: Subject,
	@Relation(
		entity = Grade::class,
		parentColumn = "subjectId",
		entityColumn = "subjectMasterId",
	)
	val schedules: List<Grade>,
)