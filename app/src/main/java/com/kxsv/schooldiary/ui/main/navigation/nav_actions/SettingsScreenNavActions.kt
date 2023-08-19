package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.ui.screens.destinations.TypedDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class SettingsScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
) : NavActions {
	fun onTermCategoryClick(typedDestination: TypedDestination<out Any?>) {
		destinationsNavigator.navigate(typedDestination.route) {
			launchSingleTop = true
		}
	}
}
