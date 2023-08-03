package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.AddEditTaskScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TaskDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class TasksScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun onAddTask() {
		destinationsNavigator.navigate(
			AddEditTaskScreenDestination(taskId = null)
		)
	}
	
	fun onTaskClick(taskId: Long) {
		destinationsNavigator.navigate(
			TaskDetailScreenDestination(taskId = taskId)
		)
	}
}
