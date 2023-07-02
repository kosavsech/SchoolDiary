package com.kxsv.schooldiary.data.features.teachers

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kxsv.schooldiary.data.features.associative_tables.subject_teacher.SubjectTeacher
import com.kxsv.schooldiary.data.features.subjects.Subject

@Entity
data class Teacher(
    val firstName: String,
    val lastName: String,
    val patronymic: String,
    val phoneNumber: String,
    @PrimaryKey(autoGenerate = true)
    val teacherId: Int = 0,
)

data class TeacherWithSubjects(
    @Embedded
    val teacher: Teacher,
    @Relation(
        entity = Subject::class,
        parentColumn = "teacherId",
        entityColumn = "subjectId",
        associateBy = Junction(SubjectTeacher::class)
    )
    val subjects: List<Subject>,
)