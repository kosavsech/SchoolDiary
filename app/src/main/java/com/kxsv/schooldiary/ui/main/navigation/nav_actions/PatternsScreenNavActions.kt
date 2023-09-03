package com.kxsv.schooldiary.ui.main.navigation.nav_actions

import androidx.annotation.StringRes
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.screens.destinations.AddEditPatternScreenDestination
import com.kxsv.schooldiary.ui.screens.patterns.PatternSelectionResult
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator


class PatternsScreenNavActions(
	override val destinationsNavigator: DestinationsNavigator,
	val resultNavigator: ResultBackNavigator<PatternSelectionResult>? = null,
) : NavActions {
	fun onAddEditPattern(patternId: Long?) {
		@StringRes val topBarTitle: Int = if (patternId == null) {
			R.string.add_pattern
		} else {
			R.string.edit_pattern
		}
		destinationsNavigator.navigate(
			AddEditPatternScreenDestination(patternId = patternId, topBarTitle = topBarTitle)
		)
	}
	
	fun navigateBackWithResult(result: PatternSelectionResult) {
		resultNavigator?.navigateBack(result)
	}
}