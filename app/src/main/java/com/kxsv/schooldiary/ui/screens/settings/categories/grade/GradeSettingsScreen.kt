package com.kxsv.schooldiary.ui.screens.settings.categories.grade

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.main.app_bars.topbar.GradeSettingsTopAppBar
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsItem
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsItemType
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.util.Utils.roundTo
import com.kxsv.schooldiary.util.Utils.stringRoundTo
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title

@Destination
@Composable
fun GradeSettingsScreen(
	destinationsNavigator: DestinationsNavigator,
	viewModel: GradeSettingsViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			GradeSettingsTopAppBar(onBack = { destinationsNavigator.popBackStack() })
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		val targetMarkDialogState = rememberMaterialDialogState(false)
		val roundRuleDialogState = rememberMaterialDialogState(false)
		
		val changeDefaultRoundRule = remember<(Double) -> Unit> {
			{ viewModel.changeDefaultRoundRule(it) }
		}
		val changeDefaultTargetMark = remember<(Double) -> Unit> {
			{ viewModel.changeDefaultTargetMark(it) }
		}
		GeneralSettingsContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			targetMarkDialogState = targetMarkDialogState,
			roundRuleDialogState = roundRuleDialogState,
			defaultTargetMark = uiState.defaultTargetMark,
			defaultRoundRule = uiState.defaultRoundRule,
		)
		
		RoundRuleDialog(
			roundRuleDialogState = roundRuleDialogState,
			defaultRoundRule = uiState.defaultRoundRule,
			onInputSave = changeDefaultRoundRule,
		)
		TargetMarkDialog(
			targetMarkDialogState = targetMarkDialogState,
			defaultTargetMark = uiState.defaultTargetMark,
			onInputSave = changeDefaultTargetMark,
		)
	}
}

@Composable
private fun TargetMarkDialog(
	targetMarkDialogState: MaterialDialogState,
	defaultTargetMark: Double?,
	onInputSave: (Double) -> Unit,
) {
	val focusManager = LocalFocusManager.current
	MaterialDialog(
		dialogState = targetMarkDialogState,
		buttons = {
			positiveButton(res = R.string.btn_save)
			negativeButton(res = R.string.btn_cancel)
		},
	) {
		title(res = R.string.enter_default_target_mark_dialog_title)
		message(res = R.string.default_target_mark_description)
		input(
			label = "Target mark",
			prefill = defaultTargetMark?.stringRoundTo(2)
				?: stringResource(id = R.string.not_found),
			placeholder = "2.89",
			isTextValid = {
				it.toDoubleOrNull() != null && (it.toDouble() > 2.00 && it.toDouble() < 5.00)
			},
			errorMessage = "Follow the format.\nAlso ensure that target is more than 2 and is less than 5",
			onInput = { onInputSave(it.toDouble().roundTo(2)) },
			waitForPositiveButton = true,
			keyboardOptions = KeyboardOptions(
				imeAction = ImeAction.Done,
				autoCorrect = false,
				capitalization = KeyboardCapitalization.None,
				keyboardType = KeyboardType.Decimal
			),
			keyboardActions = KeyboardActions(
				onDone = { focusManager.clearFocus() }
			)
		)
	}
}

@Composable
private fun RoundRuleDialog(
	roundRuleDialogState: MaterialDialogState,
	defaultRoundRule: Double?,
	onInputSave: (Double) -> Unit,
) {
	val focusManager = LocalFocusManager.current
	MaterialDialog(
		dialogState = roundRuleDialogState,
		buttons = {
			positiveButton(res = R.string.btn_save)
			negativeButton(res = R.string.btn_cancel)
		},
	) {
		val isTextValid = remember<(String) -> Boolean> {
			{ it.toDoubleOrNull() != null && (it.toDouble() > 0.44 && it.toDouble() < 1.00) }
		}
		title(res = R.string.enter_default_round_rule_dialog_title)
		message(res = R.string.default_round_rule_description)
		input(
			label = "Round rule",
			prefill = defaultRoundRule?.stringRoundTo(2) ?: stringResource(id = R.string.not_found),
			isTextValid = { isTextValid(it) },
			errorMessage = "Ensure that round rule is correct",
			onInput = { onInputSave(it.toDouble().roundTo(2)) },
			waitForPositiveButton = true,
			keyboardOptions = KeyboardOptions(
				imeAction = ImeAction.Done,
				autoCorrect = false,
				capitalization = KeyboardCapitalization.None,
				keyboardType = KeyboardType.Decimal
			),
			keyboardActions = KeyboardActions(
				onDone = { focusManager.clearFocus() }
			)
		)
	}
}

@Composable
private fun GeneralSettingsContent(
	modifier: Modifier,
	loading: Boolean,
	targetMarkDialogState: MaterialDialogState,
	roundRuleDialogState: MaterialDialogState,
	defaultTargetMark: Double?,
	defaultRoundRule: Double?,
) {
	val settingItems = listOf(
		SettingsItem(
			label = R.string.default_target_mark,
			type = SettingsItemType.Input(currentValue = defaultTargetMark?.toString()),
			onValueChange = {},
			onClick = { targetMarkDialogState.show() },
		),
		SettingsItem(
			label = R.string.default_round_rule,
			type = SettingsItemType.Input(currentValue = defaultRoundRule?.toString()),
			onValueChange = {},
			onClick = { roundRuleDialogState.show() },
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
				when (settingItem.type) {
					is SettingsItemType.Input -> {
						val clickableModifier = if (settingItem.onClick != null) {
							Modifier.clickable(
								interactionSource = remember { MutableInteractionSource() },
								indication = rememberRipple(
									bounded = false,
									radius = Dp.Unspecified,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								),
								onClick = { settingItem.onClick.invoke() }
							)
						} else Modifier
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.height(56.dp)
								.clip(MaterialTheme.shapes.extraLarge)
								.then(clickableModifier),
							verticalAlignment = Alignment.CenterVertically,
						) {
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								Text(
									text = stringResource(settingItem.label),
									style = MaterialTheme.typography.titleMedium
								)
								Text(
									text = settingItem.type.currentValue
										?: stringResource(R.string.not_found),
									style = MaterialTheme.typography.titleMedium
								)
							}
						}
					}
					
					is SettingsItemType.Toggleable -> {
						Row(
							modifier = Modifier
								.fillMaxWidth()
								.height(56.dp)
								.clip(MaterialTheme.shapes.extraLarge)
								.clickable(
									interactionSource = MutableInteractionSource(),
									indication = rememberRipple(
										bounded = false,
										radius = Dp.Unspecified,
										color = MaterialTheme.colorScheme.onSurfaceVariant
									)
								) {
									settingItem.onValueChange.invoke(
										!(settingItem.type.state ?: false)
									)
								},
							verticalAlignment = Alignment.CenterVertically,
						) {
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
								verticalAlignment = Alignment.CenterVertically,
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								Text(
									text = stringResource(settingItem.label),
									style = MaterialTheme.typography.titleMedium
								)
								Switch(
									checked = settingItem.type.state ?: false,
									onCheckedChange = settingItem.onValueChange,
									modifier = Modifier.sizeIn(maxHeight = (30.6).dp)
								)
							}
						}
					}
				}
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.list_item_padding)))
			}
		}
	}
}