package com.kxsv.schooldiary.ui.screens.task_list

import com.kxsv.schooldiary.ui.screens.destinations.AddEditTaskScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TaskDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class TasksScreenNavActions(navigator: DestinationsNavigator) {
	val onAddTask: () -> Unit = {
		navigator.navigate(
			AddEditTaskScreenDestination(taskId = null)
		)
	}
	val onTaskClick: (Long) -> Unit = { taskId ->
		navigator.navigate(
			TaskDetailScreenDestination(taskId = taskId)
		)
	}
}
