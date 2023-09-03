package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.schedule.DateRangeScheduleCopyResult
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator

class DateRangeScheduleCopyScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
	private val resultBackNavigator: ResultBackNavigator<DateRangeScheduleCopyResult>,
) : NavActions {
	fun navigateBackWithResult(result: DateRangeScheduleCopyResult) {
		resultBackNavigator.navigateBack(result)
	}
	
}
