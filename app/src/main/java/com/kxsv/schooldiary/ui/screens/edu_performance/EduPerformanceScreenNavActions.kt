package com.kxsv.schooldiary.ui.screens.edu_performance

import com.kxsv.schooldiary.ui.main.navigation.NavActions
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