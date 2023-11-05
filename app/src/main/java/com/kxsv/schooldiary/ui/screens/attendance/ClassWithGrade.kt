package com.kxsv.schooldiary.ui.screens.attendance

import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.util.Mark

data class ClassWithGrade(
	val lessonWithSubject: LessonWithSubject,
	val grade: Mark,
)
