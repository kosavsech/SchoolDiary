package com.kxsv.schooldiary.ui.screens.subject_list.subject_detail

import com.kxsv.schooldiary.ui.main.navigation.NavActions
import com.kxsv.schooldiary.ui.screens.destinations.AddEditSubjectScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator

class SubjectDetailScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
	private val resultBackNavigator: ResultBackNavigator<Int>,
) : NavActions {
	fun backWithResult(deleteResultOk: Int) {
		resultBackNavigator.navigateBack(deleteResultOk)
	}
	
	fun onEditSubject(subjectId: Long) {
		destinationsNavigator.navigate(
			AddEditSubjectScreenDestination(subjectId = subjectId)
		)
	}
}