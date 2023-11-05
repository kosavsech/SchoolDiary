package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.AddEditSubjectScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator

class AddEditLessonScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
	private val resultBackNavigator: ResultBackNavigator<Int>,
) : NavActions {
	fun navigateBackWithResult(result: Int) {
		resultBackNavigator.navigateBack(result)
	}
	
	fun onCreateSubject() {
		destinationsNavigator.navigate(
			AddEditSubjectScreenDestination(null)
		)
	}
}
