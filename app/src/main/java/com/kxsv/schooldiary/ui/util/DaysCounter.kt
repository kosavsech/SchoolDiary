package com.kxsv.schooldiary.ui.util

import androidx.annotation.StringRes

data class DaysCounter(
	@StringRes val textRes: Int,
	val value: Int?,
)
