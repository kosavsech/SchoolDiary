package com.kxsv.schooldiary.ui.screens.settings.utils

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

sealed class SettingsItemType {
	class Input(val currentValue: String?) : SettingsItemType()
	
	class Toggleable(val state: Boolean?) : SettingsItemType()
}

data class SettingsScreenItem(
	@StringRes val label: Int,
	val type: SettingsItemType,
	val onValueChange: (Any) -> Unit,
	val onClick: (() -> Unit)? = null,
	val icon: ImageVector? = null,
)