package com.kxsv.schooldiary.ui.screens.grade_list

import com.kxsv.schooldiary.ui.screens.destinations.GradeDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class GradesScreenNavActions(private val navigator: DestinationsNavigator) {
	val onGradeClick: (String) -> Unit = { gradeId ->
		navigator.navigate(
			GradeDetailScreenDestination(gradeId = gradeId)
		)
	}
}