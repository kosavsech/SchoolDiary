package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.AddEditLessonScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.DateRangeScheduleCopyScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleCopyScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.PatternsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.SubjectDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


class DayScheduleScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	
	fun onSubjectClick(subjectId: String) {
		destinationsNavigator.navigate(
			SubjectDetailScreenDestination(subjectId)
		)
	}
	
	fun onAddEditClass(datestamp: Long, lessonId: Long?) {
		destinationsNavigator.navigate(
			AddEditLessonScreenDestination(datestamp = datestamp, lessonId = lessonId)
		)
	}
	
	fun onChangePattern() {
		destinationsNavigator.navigate(PatternsScreenDestination(isSelectingMode = true))
	}
	
	fun onCopyDaySchedule() {
		destinationsNavigator.navigate(DayScheduleCopyScreenDestination)
	}
	
	fun onCopyDateRangeSchedule() {
		destinationsNavigator.navigate(DateRangeScheduleCopyScreenDestination)
	}
}