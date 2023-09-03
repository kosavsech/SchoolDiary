package com.kxsv.schooldiary.ui.util

import androidx.compose.ui.window.DialogProperties
import com.ramcosta.composedestinations.spec.DestinationStyle

object NonDismissibleDialog : DestinationStyle.Dialog {
	override val properties = DialogProperties(
		dismissOnClickOutside = false,
		dismissOnBackPress = false,
	)
}