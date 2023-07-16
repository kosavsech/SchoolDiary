package com.kxsv.schooldiary.data.local.features.associative_tables.subject_teacher

import androidx.room.Entity
import androidx.room.Index

@Entity(
    primaryKeys = ["subjectId", "teacherId"],
    indices = [Index(value = ["teacherId"])]
)
data class SubjectTeacher(
    val subjectId: Long,
    val teacherId: Int,
)