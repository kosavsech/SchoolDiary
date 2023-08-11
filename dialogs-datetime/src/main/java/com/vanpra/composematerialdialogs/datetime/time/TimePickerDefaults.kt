package com.vanpra.composematerialdialogs.datetime.time

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Object to hold default values used by [timepicker]
 */
object TimePickerDefaults {
	/**
	 * Initialises a [TimePickerColors] object which represents the different colors used by
	 * the [timepicker] composable
	 *
	 * @param activeBackgroundColor background color of selected time unit or period (AM/PM)
	 * @param inactiveBackgroundColor background color of inactive items in the dialog including
	 * the clock face
	 * @param activeTextColor color of text on the activeBackgroundColor
	 * @param inactiveTextColor color of text on the inactiveBackgroundColor
	 * @param inactivePeriodBackground background color of the inactive period (AM/PM) selector
	 * @param selectorColor color of clock hand/selector
	 * @param selectorTextColor color of text on selectedColor
	 * @param headerTextColor  Get color of title text
	 * @param borderColor border color of the period (AM/PM) selector
	 */
	@Composable
	fun colors(
		activeBackgroundColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(0.3f),
		inactiveBackgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(12.dp),
		activeTextColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
		inactiveTextColor: Color = MaterialTheme.colorScheme.onSurface,
		onInactivePeriodBackground: Color = MaterialTheme.colorScheme.onSurfaceVariant,
		inactivePeriodBackground: Color = MaterialTheme.colorScheme.surface.copy(.17f),
		activePeriodBackground: Color = MaterialTheme.colorScheme.tertiaryContainer.copy(0.3f),
		onActivePeriodBackground: Color = MaterialTheme.colorScheme.onTertiaryContainer,
		selectorColor: Color = MaterialTheme.colorScheme.primary,
		selectorTextColor: Color = MaterialTheme.colorScheme.onPrimary,
		headerTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
		borderColor: Color = MaterialTheme.colorScheme.outline,
	): TimePickerColors {
		return DefaultTimePickerColors(
			activeBackgroundColor = activeBackgroundColor,
			inactiveBackgroundColor = inactiveBackgroundColor,
			activeTextColor = activeTextColor,
			inactiveTextColor = inactiveTextColor,
			inactivePeriodBackground = inactivePeriodBackground,
			onInactivePeriodBackground = onInactivePeriodBackground,
			activePeriodBackground = activePeriodBackground,
			onActivePeriodBackground = onActivePeriodBackground,
			selectorColor = selectorColor,
			selectorTextColor = selectorTextColor,
			headerTextColor = headerTextColor,
			borderColor = borderColor
		)
	}
}
