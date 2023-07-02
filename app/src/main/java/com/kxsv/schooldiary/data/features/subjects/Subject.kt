package com.kxsv.schooldiary.data.features.subjects

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.features.schedule.Schedule
import com.kxsv.schooldiary.data.features.teachers.Teacher

@Entity
data class Subject(
    val name: String,
    val cabinet: String,
//    val tags: List<Tag>, // TODO: create many to many connection for tag and teacher
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
