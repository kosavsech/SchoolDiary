package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.GradeDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class GradesScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun onGradeClick(gradeId: String) {
		destinationsNavigator.navigate(
			GradeDetailScreenDestination(gradeId = gradeId)
		)
	}
}