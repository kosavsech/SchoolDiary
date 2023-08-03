package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.schedule.DayScheduleCopyResult
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator

class DayScheduleCopyScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
	private val resultBackNavigator: ResultBackNavigator<DayScheduleCopyResult>,
) : NavActions {
	fun backWithResult(result: DayScheduleCopyResult) {
		resultBackNavigator.navigateBack(result)
	}
	
}
