package com.kxsv.schooldiary.ui.screens.settings

import android.content.res.Configuration
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.main.app_bars.topbar.SettingsTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.SettingsScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.GeneralSettingsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.GradeSettingsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TermsSettingsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TimetableSettingsScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TypedDestination
import com.kxsv.schooldiary.ui.screens.settings.utils.SettingsScreenCategory
import com.kxsv.schooldiary.ui.theme.AppTheme
import com.kxsv.schooldiary.ui.util.AppSnackbarHost
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
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
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		topBar = {
			SettingsTopAppBar(openDrawer = { coroutineScope.launch { drawerState.open() } })
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		val onTermCategoryClick = remember<(TypedDestination<out Any?>) -> Unit> {
			{ navigator.onTermCategoryClick(it) }
		}
		val nonFunctionalCategoryClicked = remember {
			{ viewModel.nonFunctionalCategoryClicked() }
		}
		SettingsContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			onCategoryClick = onTermCategoryClick,
			nonFunctionalCategoryClicked = nonFunctionalCategoryClicked
		)
		
	}
}

@Composable
private fun SettingsContent(
	modifier: Modifier,
	loading: Boolean,
	onCategoryClick: (TypedDestination<out Any?>) -> Unit,
	nonFunctionalCategoryClicked: () -> Unit,
) {
	val settingsCategories = listOf(
		SettingsScreenCategory(
			icon = Icons.Outlined.Settings,
			label = R.string.general_settings,
			destination = GeneralSettingsScreenDestination
		),
		SettingsScreenCategory(
			icon = Icons.Outlined.CalendarMonth,
			label = R.string.terms_settings,
			destination = TermsSettingsScreenDestination
		),
		SettingsScreenCategory(
			icon = Icons.Outlined.AccessTime,
			label = R.string.timetable_settings,
			destination = TimetableSettingsScreenDestination
		),
		SettingsScreenCategory(
			icon = Icons.Outlined.Stars,
			label = R.string.grade_settings,
			destination = GradeSettingsScreenDestination
		),
	)
	LoadingContent(
		modifier = modifier,
		isLoading = loading,
		empty = false,
		isContentScrollable = false,
	) {
		Column {
			settingsCategories.forEach { settingItem ->
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
							if (settingItem.onClick != null) {
								settingItem.onClick.invoke()
							} else if (settingItem.destination != null) {
								onCategoryClick(settingItem.destination)
							} else {
								nonFunctionalCategoryClicked()
							}
						},
					verticalAlignment = Alignment.CenterVertically,
				) {
					Row(
						modifier = Modifier
							.fillMaxWidth()
							.padding(horizontal = dimensionResource(R.dimen.horizontal_margin)),
						verticalAlignment = Alignment.CenterVertically,
						horizontalArrangement = Arrangement.Start
					) {
						Icon(
							imageVector = settingItem.icon,
							contentDescription = ""
						)
						Spacer(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.list_item_padding)))
						Text(
							text = stringResource(settingItem.label),
							style = MaterialTheme.typography.titleMedium
						)
					}
				}
				
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.list_item_padding)))
			}
		}
	}
}


@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsContentPreview() {
	AppTheme(darkTheme = true) {
		Surface {
		
		}
	}
}