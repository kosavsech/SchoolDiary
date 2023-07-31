package com.kxsv.schooldiary.ui.screens.patterns

import androidx.annotation.StringRes
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.screens.destinations.AddEditPatternScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


class PatternsScreenNavActions(private val navigator: DestinationsNavigator) {
	val onAddEditPattern: (Long?) -> Unit = { patternId ->
		@StringRes val topBarTitle: Int = if (patternId == null) {
			R.string.add_pattern
		} else {
			R.string.edit_pattern
		}
		navigator.navigate(
			AddEditPatternScreenDestination(patternId = patternId, topBarTitle = topBarTitle)
		)
	}
}