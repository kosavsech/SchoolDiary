package com.kxsv.schooldiary.data.features.schedule


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kxsv.schooldiary.data.features.subjects.Subject
import java.time.LocalDate

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["subjectId"],
            childColumns = ["subjectAncestorId"],
            onDelete = ForeignKey.NO_ACTION,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class Schedule(
    val index: Int,
    val date: LocalDate,
    val subjectAncestorId: Long,
    @PrimaryKey(autoGenerate = true)
    val scheduleId: Long = 0,
)

data class ScheduleWithSubject(
    @Embedded
    val schedule: Schedule,
    @Relation(
        entity = Subject::class,
        parentColumn = "subjectAncestorId",
        entityColumn = "subjectId",
    )
    val subject: Subject
)

