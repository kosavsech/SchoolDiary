package com.kxsv.schooldiary.ui.screens.subject_list.subject_detail

import com.kxsv.schooldiary.ui.screens.destinations.AddEditSubjectScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class SubjectDetailScreenNavActions(private val navigator: DestinationsNavigator) {
	val onEditSubject: (Long) -> Unit = { subjectId ->
		navigator.navigate(
			AddEditSubjectScreenDestination(subjectId = subjectId)
		)
	}
}