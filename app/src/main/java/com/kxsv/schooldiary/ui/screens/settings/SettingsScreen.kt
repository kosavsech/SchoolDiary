package com.kxsv.schooldiary.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.main.app_bars.topbar.SettingsTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.SettingsScreenNavActions
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsItemType
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsScreenItem
import com.kxsv.schooldiary.util.Utils.stringRoundTo
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Destination
@Composable
fun SettingsScreen(
	destinationsNavigator: DestinationsNavigator,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	viewModel: SettingsViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val navigator = SettingsScreenNavActions(destinationsNavigator = destinationsNavigator)
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			SettingsTopAppBar(openDrawer = { coroutineScope.launch { drawerState.open() } })
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		val inputDialogState = rememberMaterialDialogState(false)
		
		val changeDefaultTargetMark = remember<(Double) -> Unit> {
			{ viewModel.changeDefaultTargetMark(it) }
		}
		val changeLoginSuppression = remember<(Boolean) -> Unit> {
			{ viewModel.changeLoginSuppression(it) }
		}
		SettingsContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			inputDialogState = inputDialogState,
			defaultTargetMark = uiState.defaultTargetMark,
			loginSuppression = uiState.suppressInitLogin,
			onDefaultTargetMarkChange = changeDefaultTargetMark,
			onLoginSuppressionChange = changeLoginSuppression
		)
		
		InputDialog(
			dialogState = inputDialogState,
			defaultTargetMark = uiState.defaultTargetMark,
			onInputSave = changeDefaultTargetMark,
		)
	}
}

@Composable
private fun InputDialog(
	dialogState: MaterialDialogState,
	defaultTargetMark: Double?,
	onInputSave: (Double) -> Unit,
) {
	MaterialDialog(
		dialogState = dialogState,
		buttons = {
			positiveButton(res = R.string.btn_save)
			negativeButton(res = R.string.btn_cancel)
		},
	) {
		title(res = R.string.enter_default_target_mark_dialog_title)
		input(
			label = "Target mark",
			prefill = defaultTargetMark?.stringRoundTo(2)
				?: stringResource(id = R.string.not_found),
			placeholder = "2.89",
			isTextValid = {
				it.toDoubleOrNull() != null && (it.toDouble() > 2.00 && it.toDouble() < 5.00)
			},
			errorMessage = "Follow the format.\nAlso ensure that target is more than 2 and is less than 5",
			onInput = { onInputSave(it.toDouble()) },
			waitForPositiveButton = true
		)
	}
}

@Composable
private fun SettingsContent(
	modifier: Modifier,
	loading: Boolean,
	inputDialogState: MaterialDialogState,
	defaultTargetMark: Double?,
	loginSuppression: Boolean?,
	onDefaultTargetMarkChange: (Double) -> Unit,
	onLoginSuppressionChange: (Boolean) -> Unit,
) {
	val settingItems = listOf(
		SettingsScreenItem(
			label = R.string.default_target_mark,
			type = SettingsItemType.Input(currentValue = defaultTargetMark?.toString()),
			onValueChange = { onDefaultTargetMarkChange(it as Double) },
			onClick = { inputDialogState.show() },
		),
		SettingsScreenItem(
			label = R.string.login_suppression,
			type = SettingsItemType.Toggleable(state = loginSuppression),
			onValueChange = { onLoginSuppressionChange(it as Boolean) },
		),
	)
	LoadingContent(
		modifier = modifier,
		loading = loading,
		empty = false,
		isContentScrollable = false,
	) {
		Column {
			settingItems.forEach { settingItem ->
				key(settingItem) {
					when (settingItem.type) {
						is SettingsItemType.Input -> {
							val clickableModifier = if (settingItem.onClick != null) {
								Modifier.clickable { settingItem.onClick.invoke() }
							} else {
								Modifier
							}
							Row(
								modifier = Modifier
									.then(clickableModifier)
									.heightIn(min = 48.dp)
							) {
								Text(text = stringResource(settingItem.label))
								Text(
									text = settingItem.type.currentValue
										?: stringResource(R.string.not_found)
								)
							}
						}
						
						is SettingsItemType.Toggleable -> {
							Row() {
								Text(text = stringResource(settingItem.label))
								Switch(
									checked = settingItem.type.state ?: false,
									onCheckedChange = settingItem.onValueChange
								)
							}
						}
					}
				}
			}
		}
	}
}
