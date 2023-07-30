package com.kxsv.schooldiary.ui.screens.edu_performance

import com.kxsv.schooldiary.ui.screens.destinations.SubjectDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class EduPerformanceScreenNavActions(private val navigator: DestinationsNavigator) {
	val onEduPerformanceClick: (Long) -> Unit = { subjectId ->
		navigator.navigate(
			SubjectDetailScreenDestination(subjectId = subjectId)
		)
	}
}