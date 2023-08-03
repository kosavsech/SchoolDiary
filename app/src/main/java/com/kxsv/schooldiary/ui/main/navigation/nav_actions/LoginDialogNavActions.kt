package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class LoginDialogNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun onLoggedIn() {
		destinationsNavigator.navigate(DayScheduleScreenDestination.route)
	}
}
