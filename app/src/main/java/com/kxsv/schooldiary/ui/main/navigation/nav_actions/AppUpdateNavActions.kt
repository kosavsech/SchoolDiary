package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import com.kxsv.schooldiary.data.remote.dtos.UpdateDto
import com.kxsv.schooldiary.ui.screens.destinations.MustUpdateDialogDestination
import com.kxsv.schooldiary.ui.screens.destinations.ShouldUpdateDialogDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class AppUpdateNavActions(
	override val destinationsNavigator: DestinationsNavigator,
//	private val resultNavigator: ResultBackNavigator<Int>,
) : NavActions {
	fun onAvailableUpdate(update: UpdateDto) {
		val linkToApp = update.apk.toString()
		destinationsNavigator.navigate(
			ShouldUpdateDialogDestination(linkToApp, update.version, update.releaseNotes)
		)
	}
	
	fun onMandatoryUpdate(update: UpdateDto) {
		val linkToApp = update.apk.toString()
		destinationsNavigator.navigate(
			MustUpdateDialogDestination(linkToApp, update.version, update.releaseNotes)
		)
	}
}