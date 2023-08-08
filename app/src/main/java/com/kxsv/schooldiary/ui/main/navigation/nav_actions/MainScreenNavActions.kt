package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
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
	
	fun onTaskClicked(taskId: Long) {
		destinationsNavigator.navigate(TaskDetailScreenDestination(taskId = taskId))
	}
	
	fun onTasksShowMore(date: LocalDate) {
		destinationsNavigator.navigate(TasksScreenDestination())
	}
	
	fun onScheduleShowMore() {
		destinationsNavigator.navigate(
			DayScheduleScreenDestination(
				datestamp = Utils.localDateToTimestamp(Utils.currentDate),
				showComparison = null
			)
		)
	}
}
