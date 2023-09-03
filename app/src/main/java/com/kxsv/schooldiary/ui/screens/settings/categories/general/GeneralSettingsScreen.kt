package com.kxsv.schooldiary.ui.screens.settings.categories.general

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Start
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.util.user_preferences.StartScreen
import com.kxsv.schooldiary.ui.main.app_bars.topbar.GeneralSettingsTopAppBar
import com.kxsv.schooldiary.ui.screens.settings.utils.GetSettingItemComposable
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsItem
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsItemType
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun GeneralSettingsScreen(
	destinationsNavigator: DestinationsNavigator,
	viewModel: GeneralSettingsViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			GeneralSettingsTopAppBar(onBack = { destinationsNavigator.popBackStack() })
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		val changeLoginSuppression = remember<(Boolean) -> Unit> {
			{ viewModel.changeLoginSuppression(it) }
		}
		val changeStartScreen = remember<(StartScreen) -> Unit> {
			{ viewModel.changeStartScreen(it) }
		}
		val changeCalendarScrollPaged = remember<(Boolean) -> Unit> {
			{ viewModel.changeCalendarScrollPaged(it) }
		}
		GeneralSettingsContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			loginSuppression = uiState.suppressInitLogin,
			onLoginSuppressionChange = changeLoginSuppression,
			currentStartScreen = uiState.startScreen,
			changeStartScreen = changeStartScreen,
			calendarScrollPaged = uiState.calendarScrollPaged,
			onCalendarScrollPagedChange = changeCalendarScrollPaged
		)
		
	}
}

@Composable
private fun GeneralSettingsContent(
	modifier: Modifier,
	loading: Boolean,
	loginSuppression: Boolean?,
	onLoginSuppressionChange: (Boolean) -> Unit,
	currentStartScreen: StartScreen?,
	changeStartScreen: (StartScreen) -> Unit,
	calendarScrollPaged: Boolean?,
	onCalendarScrollPagedChange: (Boolean) -> Unit,
) {
	val settingItems = listOf(
		SettingsItem(
			label = R.string.current_start_screen,
			type = SettingsItemType.DropdownChoice(
				currentValue = currentStartScreen,
				choiceOptions = StartScreen.values()
			),
			onValueChange = { changeStartScreen(it as StartScreen) },
			icon = Icons.Default.Start
		),
		SettingsItem(
			label = R.string.login_suppression,
			type = SettingsItemType.Toggleable(currentState = loginSuppression),
			onValueChange = { onLoginSuppressionChange(it as Boolean) },
		),
		SettingsItem(
			label = R.string.calendar_scroll_paged,
			type = SettingsItemType.Toggleable(currentState = calendarScrollPaged),
			onValueChange = { onCalendarScrollPagedChange(it as Boolean) },
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
				GetSettingItemComposable(settingItem)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.list_item_padding)))
			}
		}
	}
}
