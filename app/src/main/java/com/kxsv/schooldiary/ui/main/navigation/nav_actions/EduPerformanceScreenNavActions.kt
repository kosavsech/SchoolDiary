package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.SubjectDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class EduPerformanceScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun onEduPerformanceClick(subjectId: Long) {
		destinationsNavigator.navigate(
			SubjectDetailScreenDestination(subjectId = subjectId)
		)
	}
}