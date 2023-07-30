package com.kxsv.schooldiary.ui.screens.subject_list

import com.kxsv.schooldiary.ui.screens.destinations.AddEditSubjectScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.SubjectDetailScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class SubjectsScreenNavActions(navigator: DestinationsNavigator) {
	val onAddSubject: () -> Unit = {
		navigator.navigate(
			AddEditSubjectScreenDestination(subjectId = null)
		)
	}
	val onSubjectClick: (Long) -> Unit = { subjectId ->
		navigator.navigate(
			SubjectDetailScreenDestination(subjectId = subjectId)
		)
		
	}
}
