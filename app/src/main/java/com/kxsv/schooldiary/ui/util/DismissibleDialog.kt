package com.kxsv.schooldiary.ui.util

import androidx.compose.ui.window.DialogProperties
import com.ramcosta.composedestinations.spec.DestinationStyle

object DismissibleDialog : DestinationStyle.Dialog {
	override val properties = DialogProperties(
		dismissOnClickOutside = true,
		dismissOnBackPress = true,
	)
}