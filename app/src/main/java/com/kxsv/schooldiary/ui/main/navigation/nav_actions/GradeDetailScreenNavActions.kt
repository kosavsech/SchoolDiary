package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.SubjectDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class GradeDetailScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun onSubjectClick(subjectId: String) {
		destinationsNavigator.navigate(
			SubjectDetailScreenDestination(subjectId = subjectId)
		)
	}
}
