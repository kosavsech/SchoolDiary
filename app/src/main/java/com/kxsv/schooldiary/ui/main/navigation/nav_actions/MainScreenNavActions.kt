package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.AddEditLessonScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.SubjectDetailScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TaskDetailScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TasksScreenDestination
import com.kxsv.schooldiary.util.Utils
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.time.LocalDate

class MainScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun chipNavigate(route: String) {
		destinationsNavigator.navigate(route)
	}
	
	fun onTaskClicked(taskId: String) {
		destinationsNavigator.navigate(TaskDetailScreenDestination(taskId = taskId))
	}
	
	fun onTasksShowMore(date: LocalDate) {
		destinationsNavigator.navigate(
			TasksScreenDestination(dateStamp = Utils.localDateToDatestamp(date))
		)
	}
	
	fun onAddEditClass(lessonId: Long?) {
		destinationsNavigator.navigate(
			AddEditLessonScreenDestination(datestamp = 0, lessonId = lessonId)
		)
	}
	
	fun onScheduleShowMore() {
		destinationsNavigator.navigate(
			DayScheduleScreenDestination(
				datestamp = Utils.localDateToDatestamp(Utils.currentDate),
				showComparison = null
			)
		)
	}
	
	fun onSubjectClick(subjectId: String) {
		destinationsNavigator.navigate(
			SubjectDetailScreenDestination(subjectId)
		)
	}
}
