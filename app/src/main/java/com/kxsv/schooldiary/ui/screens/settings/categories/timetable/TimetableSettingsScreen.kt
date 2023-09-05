package com.kxsv.schooldiary.ui.screens.settings.categories.timetable

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
import com.kxsv.schooldiary.ui.main.app_bars.topbar.TimetableSettingsTopAppBar
import com.kxsv.schooldiary.ui.screens.settings.utils.GetSettingItemComposable
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsItem
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsItemType
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.util.Utils.AppSnackbarHost
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
fun TimetableSettingsScreen(
	destinationsNavigator: DestinationsNavigator,
	viewModel: TimetableSettingsViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	Scaffold(
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		topBar = {
			TimetableSettingsTopAppBar(onBack = { destinationsNavigator.popBackStack() })
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		val lessonDurationDialogState = rememberMaterialDialogState(false)
		
		val changeDefaultLessonDuration = remember<(Long) -> Unit> {
			{ viewModel.changeDefaultLessonDuration(it) }
		}
		TimetableSettingsContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			lessonDurationDialogState = lessonDurationDialogState,
			defaultLessonDuration = uiState.defaultLessonDuration,
		)
		
		LessonDurationDialog(
			lessonDurationDialogState = lessonDurationDialogState,
			defaultLessonDuration = uiState.defaultLessonDuration,
			onInputSave = changeDefaultLessonDuration,
		)
	}
}

@Composable
private fun LessonDurationDialog(
	lessonDurationDialogState: MaterialDialogState,
	defaultLessonDuration: Long?,
	onInputSave: (Long) -> Unit,
) {
	val focusManager = LocalFocusManager.current
	MaterialDialog(
		dialogState = lessonDurationDialogState,
		buttons = {
			positiveButton(res = R.string.btn_save)
			negativeButton(res = R.string.btn_cancel)
		},
	) {
		title(res = R.string.enter_default_lesson_duration_dialog_title)
		message(res = R.string.default_lesson_duration_description)
		input(
			label = "Default lesson duration",
			prefill = defaultLessonDuration?.toString() ?: stringResource(id = R.string.not_found),
			isTextValid = {
				it.toLongOrNull() != null && (it.toLong() in 20..60)
			},
			errorMessage = "Ensure that duration is at least 20 and not more than 60",
			onInput = { onInputSave(it.toLong()) },
			waitForPositiveButton = true,
			keyboardOptions = KeyboardOptions(
				imeAction = ImeAction.Done,
				autoCorrect = false,
				capitalization = KeyboardCapitalization.None,
				keyboardType = KeyboardType.NumberPassword
			),
			keyboardActions = KeyboardActions(
				onDone = { focusManager.clearFocus() }
			)
		)
	}
}


@Composable
private fun TimetableSettingsContent(
	modifier: Modifier,
	loading: Boolean,
	lessonDurationDialogState: MaterialDialogState,
	defaultLessonDuration: Long?,
) {
	val settingItems = listOf(
		SettingsItem(
			label = R.string.default_lesson_duration,
			type = SettingsItemType.Input(currentValue = defaultLessonDuration?.toString()),
			onValueChange = {},
			onClick = { lessonDurationDialogState.show() },
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
				GetSettingItemComposable(settingItem = settingItem)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.list_item_padding)))
			}
		}
	}
}