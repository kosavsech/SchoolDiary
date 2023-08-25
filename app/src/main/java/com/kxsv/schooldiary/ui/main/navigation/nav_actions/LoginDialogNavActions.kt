package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.data.util.user_preferences.StartScreen
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class LoginDialogNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun onLoggedIn(startScreen: StartScreen) {
		destinationsNavigator.navigate(startScreen.route.route)
	}
}
