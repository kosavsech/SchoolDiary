package com.kxsv.schooldiary.ui.screens.settings.categories.grade

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.main.app_bars.topbar.GradeSettingsTopAppBar
import com.kxsv.schooldiary.ui.screens.settings.utils.GetSettingItemComposable
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsItem
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsItemType
import com.kxsv.schooldiary.ui.util.AppSnackbarHost
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.util.Extensions.roundTo
import com.kxsv.schooldiary.util.Extensions.stringRoundTo
import com.kxsv.ychart_mod.common.extensions.isNotNull
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
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		topBar = {
			GradeSettingsTopAppBar(onBack = { destinationsNavigator.popBackStack() })
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		val targetMarkDialogState = rememberMaterialDialogState(false)
		val lowerBoundMarkDialogState = rememberMaterialDialogState(false)
		val roundRuleDialogState = rememberMaterialDialogState(false)
		
		GeneralSettingsContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			targetMarkDialogState = targetMarkDialogState,
			lowerBoundMarkDialogState = lowerBoundMarkDialogState,
			roundRuleDialogState = roundRuleDialogState,
			defaultTargetMark = uiState.defaultTargetMark,
			defaultLowerBoundMark = uiState.defaultLowerBoundMark,
			defaultRoundRule = uiState.defaultRoundRule,
		)
		
		val changeDefaultRoundRule = remember<(Double) -> Unit> {
			{ viewModel.changeDefaultRoundRule(it) }
		}
		RoundRuleDialog(
			roundRuleDialogState = roundRuleDialogState,
			defaultRoundRule = uiState.defaultRoundRule,
			onInputSave = changeDefaultRoundRule,
		)
		
		val changeDefaultTargetMark = remember<(Double) -> Unit> {
			{ viewModel.changeDefaultTargetMark(it) }
		}
		MarkDialog(
			markDialogState = targetMarkDialogState,
			prefillMark = uiState.defaultTargetMark,
			onInputSave = changeDefaultTargetMark,
			dialogTitleRes = R.string.enter_default_target_mark_dialog_title,
			inputLabelRes = R.string.label_target_mark,
			descriptionRes = R.string.default_target_mark_description,
		)
		
		val changeDefaultLowerBoundMark = remember<(Double) -> Unit> {
			{ viewModel.changeDefaultLowerBoundMark(it) }
		}
		MarkDialog(
			markDialogState = lowerBoundMarkDialogState,
			prefillMark = uiState.defaultLowerBoundMark,
			onInputSave = changeDefaultLowerBoundMark,
			dialogTitleRes = R.string.enter_default_lower_bound_mark_dialog_title,
			inputLabelRes = R.string.label_lower_bound_mark,
			descriptionRes = R.string.default_lower_bound_mark_description,
		)
	}
}

@Composable
private fun MarkDialog(
	markDialogState: MaterialDialogState,
	prefillMark: Double?,
	onInputSave: (Double) -> Unit,
	@StringRes dialogTitleRes: Int,
	@StringRes inputLabelRes: Int,
	@StringRes placeholderRes: Int = R.string.place_holder_mark,
	@StringRes errorRes: Int = R.string.error_msg_mark_input,
	@StringRes descriptionRes: Int? = null,
) {
	val focusManager = LocalFocusManager.current
	MaterialDialog(
		dialogState = markDialogState,
		buttons = {
			positiveButton(res = R.string.btn_save)
			negativeButton(res = R.string.btn_cancel)
		},
	) {
		title(res = dialogTitleRes)
		if (descriptionRes.isNotNull()) {
			message(res = descriptionRes)
		}
		input(
			label = stringResource(id = inputLabelRes),
			prefill = prefillMark?.stringRoundTo(2) ?: stringResource(id = R.string.not_found),
			placeholder = stringResource(id = placeholderRes),
			isTextValid = {
				it.toDoubleOrNull() != null && (it.toDouble() > 2.00 && it.toDouble() < 5.00)
			},
			errorMessage = stringResource(id = errorRes),
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
	lowerBoundMarkDialogState: MaterialDialogState,
	roundRuleDialogState: MaterialDialogState,
	defaultTargetMark: Double?,
	defaultLowerBoundMark: Double?,
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
			label = R.string.default_lower_bound_mark,
			type = SettingsItemType.Input(currentValue = defaultLowerBoundMark?.toString()),
			onValueChange = {},
			onClick = { lowerBoundMarkDialogState.show() },
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
		isLoading = loading,
		empty = false,
		isContentScrollable = false,
	) {
		Column {
			settingItems.forEach { settingItem ->
				GetSettingItemComposable(settingItem)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.list_item_padding)))
			}
		}
	}
}