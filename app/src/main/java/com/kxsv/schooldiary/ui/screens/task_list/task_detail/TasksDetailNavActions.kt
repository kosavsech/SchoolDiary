package com.kxsv.schooldiary.ui.screens.task_list.task_detail

import com.kxsv.schooldiary.ui.screens.destinations.AddEditTaskScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class TasksDetailNavActions(navigator: DestinationsNavigator) {
	val onEditTask: (Long) -> Unit = { taskId ->
		navigator.navigate(
			AddEditTaskScreenDestination(
				taskId = taskId
			)
		)
	}
	
}
