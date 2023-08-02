package com.kxsv.schooldiary.ui.main.navigation

import com.ramcosta.composedestinations.navigation.DestinationsNavigator

interface NavActions {
	val destinationsNavigator: DestinationsNavigator
	
	fun popBackStack() = destinationsNavigator.popBackStack()
}