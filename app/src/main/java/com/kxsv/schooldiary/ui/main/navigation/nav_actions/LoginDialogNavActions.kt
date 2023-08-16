package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.MainScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class LoginDialogNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun onLoggedIn() {
		destinationsNavigator.navigate(MainScreenDestination)
	}
}
