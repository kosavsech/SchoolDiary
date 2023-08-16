package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator

class AddEditTaskScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
	private val resultNavigator: ResultBackNavigator<Int>,
) : NavActions {
	fun backWithResult(result: Int) {
		resultNavigator.navigateBack(result, true)
	}
}
