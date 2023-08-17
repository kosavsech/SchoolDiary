package com.kxsv.schooldiary.ui.screens.main_screen

import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.task.TaskWithSubject
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import java.time.LocalDate

data class MainScreenItem(
	val date: LocalDate,
	val classes: Map<Int, LessonWithSubject> = emptyMap(),
	val tasks: List<TaskWithSubject> = emptyList(),
	val pattern: List<PatternStrokeEntity> = emptyList(),
)