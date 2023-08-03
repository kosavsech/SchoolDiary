package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.AddEditTaskScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator

class TasksDetailNavActions(
	override val destinationsNavigator: DestinationsNavigator,
	private val resultBackNavigator: ResultBackNavigator<Int>,
) : NavActions {
	fun onEditTask(taskId: Long) {
		destinationsNavigator.navigate(
			AddEditTaskScreenDestination(
				taskId = taskId
			)
		)
	}
	
	fun backWithResult(result: Int) {
	
	}
}
