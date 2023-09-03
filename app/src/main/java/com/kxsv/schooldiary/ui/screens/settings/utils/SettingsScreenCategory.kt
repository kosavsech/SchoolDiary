package com.kxsv.schooldiary.ui.screens.settings.utils

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.kxsv.schooldiary.ui.screens.destinations.TypedDestination

data class SettingsScreenCategory(
	val icon: ImageVector,
	@StringRes val label: Int,
	val destination: TypedDestination<out Any?>? = null,
	val onClick: (() -> Unit)? = null,
)