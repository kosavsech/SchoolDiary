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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.kxsv.schooldiary.BuildConfig
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.theme.AppTheme
import com.kxsv.schooldiary.ui.util.DismissibleDialog
import com.kxsv.schooldiary.ui.util.Indicator
import com.pouyaheydari.appupdater.core.pojo.DialogStates
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.customView
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
			customView {
				Column(
					modifier = Modifier.verticalScroll(rememberScrollState())
				) {
					Text(
						text = stringResource(R.string.current_version, BuildConfig.VERSION_NAME),
						style = MaterialTheme.typography.bodyMedium
					)
					Text(
						text = stringResource(R.string.update_version, version),
						style = MaterialTheme.typography.bodyMedium
					)
					Text(
						text = releaseNotes,
						color = MaterialTheme.colorScheme.onSurface,
						style = MaterialTheme.typography.bodyLarge,
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

@Preview
@Composable
fun UpdateDialogContentPreview() {
	AppTheme {
		Surface {
			val dialogProperties by remember { mutableStateOf(DismissibleDialog.properties) }
			UpdateDialogContent(
				dialogProperties = dialogProperties,
				dialogState = DialogStates.ShowUpdateInProgress,
				onDismissClick = {},
				onUpdateClick = {},
				releaseNotes = "Fixed:\norder of recent grades in subject detailed\nempty line of cabinet of lesson in schedule screen\nnow fixed width of subject column in report card screen\nnow fixed width of subject column in grades\ncabinet distinction on main screen, on schedule screen\nstatus bar color on light theme glitch\ncolor of weekline in schedule screen, also adjusted height of this line\nremoved year from dates in notifications\nmore intelligent new fetched task notification shortening\n\nAdded:\nswitcher to bad grades estimated(ruin of current / or lower bound)\ngrade avg predict\ncalculation of days left\ncalculation of lessons left\nability to go to subject details from lesson detailed in schedule screen ALSO swap EDIT DELETE buttons\nability to go to subject details from grade detailed\nability to create subject right from subject selection menu\nteachers screen ui improved\n",
				version = "0.2.1-alpha",
				title = R.string.new_version_found
			)
		}
	}
}