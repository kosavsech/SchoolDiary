package com.kxsv.schooldiary.ui.screens.subject_list

import com.kxsv.schooldiary.ui.main.navigation.NavActions
import com.kxsv.schooldiary.ui.screens.destinations.AddEditSubjectScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.SubjectDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class SubjectsScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun onAddSubject() {
		destinationsNavigator.navigate(
			AddEditSubjectScreenDestination(subjectId = null)
		)
	}
	
	fun onSubjectClick(subjectId: Long) {
		destinationsNavigator.navigate(
			SubjectDetailScreenDestination(subjectId = subjectId)
		)
	}
}
