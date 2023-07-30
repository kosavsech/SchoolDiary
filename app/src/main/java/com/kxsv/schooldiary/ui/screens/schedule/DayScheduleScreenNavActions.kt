package com.kxsv.schooldiary.ui.screens.schedule

import com.kxsv.schooldiary.ui.screens.destinations.AddEditLessonScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.DateRangeScheduleCopyScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleCopyScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.PatternSelectionScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class DayScheduleScreenNavActions(private val navigator: DestinationsNavigator) {
	val onAddEditClass: (Long, Long?) -> Unit = { datestamp, lessonId ->
		navigator.navigate(
			AddEditLessonScreenDestination(datestamp = datestamp, lessonId = lessonId)
		)
	}
	val onChangePattern: () -> Unit = { ->
		navigator.navigate(PatternSelectionScreenDestination())
	}
	val onCopyDaySchedule: () -> Unit = {
		navigator.navigate(
			DayScheduleCopyScreenDestination
		)
	}
	val onCopyDateRangeSchedule: () -> Unit = {
		navigator.navigate(
			DateRangeScheduleCopyScreenDestination
		)
	}
}