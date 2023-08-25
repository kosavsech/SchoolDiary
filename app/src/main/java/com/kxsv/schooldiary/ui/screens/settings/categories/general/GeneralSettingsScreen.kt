package com.kxsv.schooldiary.ui.screens.settings.categories.general

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Start
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.util.user_preferences.StartScreen
import com.kxsv.schooldiary.ui.main.app_bars.topbar.GeneralSettingsTopAppBar
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
		GeneralSettingsContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			loginSuppression = uiState.suppressInitLogin,
			onLoginSuppressionChange = changeLoginSuppression,
			currentStartScreen = uiState.startScreen,
			changeStartScreen = changeStartScreen
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
) {
	val settingItems = listOf(
		SettingsItem(
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
			StartScreenItem(currentStartScreen, changeStartScreen)
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

@Composable
private fun StartScreenItem(
	currentStartScreen: StartScreen?,
	changeStartScreen: (StartScreen) -> Unit,
) {
	var expanded by remember { mutableStateOf(false) }
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { expanded = true }
			.padding(dimensionResource(R.dimen.vertical_margin)),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Default.Start,
			contentDescription = "Start screen",
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin)))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				text = stringResource(R.string.current_start_screen),
				style = MaterialTheme.typography.titleMedium,
			)
			val currentStartScreenRes = currentStartScreen?.textRes ?: R.string.something_went_wrong
			Text(
				text = stringResource(currentStartScreenRes),
				style = MaterialTheme.typography.titleMedium,
			)
		}
		val configuration = LocalConfiguration.current
		val screenWidth = (configuration.screenWidthDp.dp / 3) * 2
		
		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false },
			offset = DpOffset(screenWidth, 10.dp),
		) {
			StartScreen.values().forEach {
				val isSelected = currentStartScreen == it
				val backgroundModifier = if (isSelected) {
					Modifier.background(MaterialTheme.colorScheme.outlineVariant)
				} else Modifier
				DropdownMenuItem(
					text = { Text(text = stringResource(it.textRes)) },
					onClick = { changeStartScreen(it); expanded = false },
					enabled = !isSelected,
					modifier = Modifier.then(backgroundModifier),
					colors = MenuDefaults.itemColors(
						textColor = MaterialTheme.colorScheme.onSurface,
						disabledTextColor = MaterialTheme.colorScheme.onSurface,
					)
				)
			}
		}
		
	}
}
