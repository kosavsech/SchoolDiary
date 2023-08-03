package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator

class AddEditSubjectScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
	private val resultBackNavigator: ResultBackNavigator<Int>,
) : NavActions {
	fun navigateBackWithResult(result: Int) {
		resultBackNavigator.navigateBack(result)
	}
	
}
