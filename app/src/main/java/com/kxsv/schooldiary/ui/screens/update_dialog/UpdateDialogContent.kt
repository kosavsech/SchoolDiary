package com.kxsv.schooldiary.ui.screens.update_dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.kxsv.schooldiary.BuildConfig
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.util.Indicator
import com.pouyaheydari.appupdater.core.pojo.DialogStates
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.customView
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title

@Composable
internal fun UpdateDialogContent(
	dialogProperties: DialogProperties,
	dialogState: DialogStates,
	@StringRes title: Int,
	releaseNotes: String,
	version: String,
	onUpdateClick: () -> Unit,
	onDismissClick: () -> Unit,
) {
	val materialDialogState = rememberMaterialDialogState(true)
	Box(
		modifier = Modifier.fillMaxSize(),
	) {
		MaterialDialog(
			dialogState = materialDialogState,
			properties = dialogProperties,
			buttons = {
				positiveButton(
					res = R.string.btn_update,
					onClick = onUpdateClick
				)
				if (dialogProperties.dismissOnClickOutside) {
					negativeButton(res = R.string.btn_dismiss, onClick = onDismissClick)
				}
			},
			autoDismiss = false,
			onCloseRequest = { onDismissClick() }
		) {
			PositiveButtonEnabled(
				valid = dialogState is DialogStates.HideUpdateInProgress,
				onDispose = {}
			)
			title(res = title)
			message(text = releaseNotes)
			customView {
				Column {
					Text(
						text = stringResource(
							R.string.current_version,
							BuildConfig.VERSION_NAME
						),
						style = MaterialTheme.typography.bodyMedium
					)
					Text(
						text = stringResource(R.string.update_version, version),
						style = MaterialTheme.typography.bodyMedium
					)
					if (dialogState is DialogStates.ShowUpdateInProgress) {
						Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.End,
							verticalAlignment = Alignment.CenterVertically,
						) {
							Indicator(size = 32.dp)
							Column {
								Text(
									text = stringResource(R.string.update_in_progress),
									style = MaterialTheme.typography.bodyMedium,
									modifier = Modifier
										.padding(horizontal = 24.dp)
								)
								if (dialogProperties.dismissOnClickOutside) {
									Text(
										text = stringResource(R.string.dismiss_available),
										style = MaterialTheme.typography.labelMedium,
										modifier = Modifier
											.padding(horizontal = 24.dp)
									)
								}
							}
						}
					}
				}
			}
		}
	}
}