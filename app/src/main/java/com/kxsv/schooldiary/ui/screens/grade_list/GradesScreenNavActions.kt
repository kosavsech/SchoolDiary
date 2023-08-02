package com.kxsv.schooldiary.ui.screens.grade_list

import com.kxsv.schooldiary.ui.main.navigation.NavActions
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