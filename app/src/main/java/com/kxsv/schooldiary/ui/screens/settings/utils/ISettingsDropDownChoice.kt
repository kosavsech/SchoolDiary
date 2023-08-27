package com.kxsv.schooldiary.ui.screens.settings.utils

import androidx.annotation.StringRes

interface ISettingsDropDownChoice {
	val value: Any
	
	@get:StringRes
	val textRes: Int
}