package com.kxsv.schooldiary.ui.screens.settings.categories.terms

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.util.user_preferences.Period.Companion.getTypeByPeriod
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.data.util.user_preferences.PeriodWithRange
import com.kxsv.schooldiary.ui.main.app_bars.topbar.TermsSettingsTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.kxsv.schooldiary.util.Utils.AppSnackbarHost
import com.kxsv.schooldiary.util.Utils.localDateToPeriodRangeEntry
import com.kxsv.schooldiary.util.Utils.periodRangeEntryToLocalDate
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.ResultBackNavigator
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.collections.immutable.PersistentList
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val formatter = DateTimeFormatter.ofPattern("dd MMM")
private const val TAG = "TermsSettingsScreen"

@Destination
@Composable
fun TermsSettingsScreen(
	destinationsNavigator: DestinationsNavigator,
	resultBackNavigator: ResultBackNavigator<Int>,
	viewModel: TermsSettingsViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val saveTermsSettings = remember {
		{ viewModel.save() }
	}
	LaunchedEffect(uiState.isSaved) {
		if (uiState.isSaved) {
			resultBackNavigator.navigateBack(EDIT_RESULT_OK)
		}
	}
	Scaffold(
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		topBar = {
			TermsSettingsTopAppBar(onBack = { destinationsNavigator.popBackStack() })
		},
		floatingActionButton = {
			FloatingActionButton(onClick = saveTermsSettings) {
				Icon(
					imageVector = Icons.Filled.Save,
					contentDescription = stringResource(R.string.save_schedule)
				)
			}
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		val dialogState = rememberMaterialDialogState(false)
		val startDateDialogState = rememberMaterialDialogState(false)
		val endDateDialogState = rememberMaterialDialogState(false)
		
		val changeEducationPeriodType = remember<(PeriodType) -> Unit> {
			{ viewModel.changeEducationPeriodType(it) }
		}
		val onEntryClick = remember<(PeriodWithRange) -> Unit> {
			{ viewModel.selectEntry(it); dialogState.show() }
		}
		TermsSettingsContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			currentPeriodType = uiState.educationPeriodType,
			allPeriodRanges = uiState.allPeriodRanges,
			changeEducationPeriodType = changeEducationPeriodType,
			onEntryClick = onEntryClick,
		)
		
		val savePeriod = remember {
			{ viewModel.savePeriodsRanges() }
		}
		val onCancel = remember {
			{ viewModel.unselectEntry() }
		}
		val clearErrorMessage = remember {
			{ viewModel.clearErrorMessage() }
		}
		LaunchedEffect(uiState.isPeriodSaved) {
			if (uiState.isPeriodSaved) dialogState.hide()
		}
		PeriodRangeEntryDialog(
			dialogState = dialogState,
			startDateDialogState = startDateDialogState,
			endDateDialogState = endDateDialogState,
			titleRes = when (uiState.educationPeriodType) {
				PeriodType.TERMS -> R.string.configure_term
				PeriodType.SEMESTERS -> R.string.configure_semester
				else -> R.string.corrupted_class
			},
			periodWithRangeEntry = uiState.periodWithRangeToUpdate,
			errorMessage = uiState.errorMessage,
			clearErrorMessage = clearErrorMessage,
			savePeriod = savePeriod,
			onCancel = onCancel
		)
		
		val updateEntry = remember<(PeriodWithRange) -> Unit> {
			{ viewModel.updateEntry(it) }
		}
		if (uiState.periodWithRangeToUpdate != null) {
			DatePickerDialog(
				dialogState = startDateDialogState,
				titleRes = R.string.choose_start_of_period_title,
				onDateChanged = {
					val updatedRange = uiState.periodWithRangeToUpdate.range.copy(
						start = localDateToPeriodRangeEntry(it)
					)
					val updatedEntry = uiState.periodWithRangeToUpdate.copy(range = updatedRange)
					updateEntry(updatedEntry)
				},
				initialDate = periodRangeEntryToLocalDate(uiState.periodWithRangeToUpdate.range.start)
			)
			DatePickerDialog(
				dialogState = endDateDialogState,
				titleRes = R.string.choose_end_of_period_title,
				onDateChanged = {
					val updatedRange =
						uiState.periodWithRangeToUpdate.range.copy(
							end = localDateToPeriodRangeEntry(it)
						)
					val updatedEntry = uiState.periodWithRangeToUpdate.copy(range = updatedRange)
					updateEntry(updatedEntry)
				},
				initialDate = periodRangeEntryToLocalDate(uiState.periodWithRangeToUpdate.range.end)
			)
		}
	}
}

@Composable
private fun TermsSettingsContent(
	modifier: Modifier,
	loading: Boolean,
	currentPeriodType: PeriodType?,
	allPeriodRanges: PersistentList<PeriodWithRange>?,
	changeEducationPeriodType: (PeriodType) -> Unit,
	onEntryClick: (PeriodWithRange) -> Unit,
) {
	LoadingContent(
		modifier = modifier,
		loading = loading,
		empty = (allPeriodRanges.isNullOrEmpty() && currentPeriodType == null)
	) {
		if (currentPeriodType != null && allPeriodRanges != null) {
			Column {
				PeriodTypeItem(
					currentPeriodType = currentPeriodType,
					changeEducationPeriodType = changeEducationPeriodType
				)
				val periodRanges = remember(currentPeriodType, allPeriodRanges) {
					allPeriodRanges.filter {
						getTypeByPeriod(it.period) == currentPeriodType
					}
				}
				periodRanges.forEach {
					key(it, currentPeriodType) {
						PeriodItem(
							periodWithRangeEntry = it,
							educationPeriodType = currentPeriodType
						) {
							onEntryClick(it)
						}
					}
				}
			}
		}
	}
}

@Composable
private fun PeriodTypeItem(
	currentPeriodType: PeriodType,
	changeEducationPeriodType: (PeriodType) -> Unit,
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
			imageVector = Icons.Default.EditCalendar,
			contentDescription = "Period type",
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin)))
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				text = stringResource(R.string.current_education_period_type),
				style = MaterialTheme.typography.titleMedium,
			)
			val currentEducationPeriodTypeRes = when (currentPeriodType) {
				PeriodType.TERMS -> R.string.term
				PeriodType.SEMESTERS -> R.string.semester
			}
			Text(
				text = stringResource(currentEducationPeriodTypeRes),
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
			PeriodType.values().forEach {
				val isSelected = currentPeriodType == it
				val backgroundModifier = if (isSelected) {
					Modifier.background(MaterialTheme.colorScheme.outlineVariant)
				} else Modifier
				val periodTypeRes = when (it) {
					PeriodType.TERMS -> R.string.term
					PeriodType.SEMESTERS -> R.string.semester
				}
				DropdownMenuItem(
					text = { Text(text = stringResource(periodTypeRes)) },
					onClick = { changeEducationPeriodType(it); expanded = false },
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

@Composable
private fun PeriodItem(
	periodWithRangeEntry: PeriodWithRange,
	educationPeriodType: PeriodType,
	onEntryClick: () -> Unit,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onEntryClick() }
			.padding(dimensionResource(R.dimen.vertical_margin)),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Default.CalendarToday,
			contentDescription = "Term",
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin)))
		Column {
			val ordinal =
				stringArrayResource(R.array.ordinals)[periodWithRangeEntry.period.index] + " "
			val periodRes =
				if (educationPeriodType != PeriodType.TERMS) R.string.semester else R.string.term
			Text(
				text = "$ordinal " + stringResource(periodRes),
				style = MaterialTheme.typography.titleMedium,
			)
			val startRange = periodRangeEntryToLocalDate(periodWithRangeEntry.range.start)
				.format(formatter)
			val endRange = periodRangeEntryToLocalDate(periodWithRangeEntry.range.end)
				.format(formatter)
			Text(
				text = "$startRange - $endRange",
				style = MaterialTheme.typography.labelLarge,
			)
		}
		
	}
}

@Composable
private fun PeriodRangeEntryDialog(
	dialogState: MaterialDialogState,
	startDateDialogState: MaterialDialogState,
	endDateDialogState: MaterialDialogState,
	@StringRes titleRes: Int,
	periodWithRangeEntry: PeriodWithRange?,
	@StringRes errorMessage: Int?,
	clearErrorMessage: () -> Unit,
	savePeriod: () -> Unit,
	onCancel: () -> Unit,
) {
	if (periodWithRangeEntry != null) {
		LaunchedEffect(periodWithRangeEntry.range) { clearErrorMessage() }
	}
	if (periodWithRangeEntry != null) {
		MaterialDialog(
			dialogState = dialogState,
			buttons = {
				positiveButton(res = R.string.btn_save, onClick = savePeriod)
				negativeButton(
					res = R.string.btn_cancel,
					onClick = { onCancel(); dialogState.hide() })
			},
			autoDismiss = false
		) {
			val ordinal =
				stringArrayResource(R.array.ordinals)[periodWithRangeEntry.period.index] + " "
			title(stringResource(titleRes, ordinal))
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { startDateDialogState.show() }
					.padding(
						vertical = dimensionResource(R.dimen.vertical_margin),
						horizontal = 24.dp
					)
			) {
				Text(
					text = stringResource(id = R.string.start_date_label),
					style = MaterialTheme.typography.labelLarge,
				)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
				Text(
					text = periodRangeEntryToLocalDate(periodWithRangeEntry.range.start)
						.format(formatter),
					style = MaterialTheme.typography.bodyLarge
				)
			}
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.clickable { endDateDialogState.show() }
					.padding(
						vertical = dimensionResource(R.dimen.vertical_margin),
						horizontal = 24.dp
					)
			) {
				Text(
					text = stringResource(id = R.string.end_date_label),
					style = MaterialTheme.typography.labelLarge,
				)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
				Text(
					text = periodRangeEntryToLocalDate(periodWithRangeEntry.range.end)
						.format(formatter),
					style = MaterialTheme.typography.bodyLarge
				)
			}
			if (errorMessage != null) {
				Text(
					text = stringResource(errorMessage),
					style = MaterialTheme.typography.labelMedium,
					color = MaterialTheme.colorScheme.error,
					modifier = Modifier
						.padding(horizontal = dimensionResource(id = R.dimen.horizontal_margin))
				)
			}
		}
	}
}

@Composable
private fun DatePickerDialog(
	dialogState: MaterialDialogState,
	@StringRes titleRes: Int,
	onDateChanged: (LocalDate) -> Unit,
	initialDate: LocalDate,
	allowedDateValidator: (LocalDate) -> Boolean = { true },
) {
	MaterialDialog(
		dialogState = dialogState,
		buttons = {
			positiveButton(res = R.string.btn_select)
			negativeButton(res = R.string.btn_cancel)
		}
	) {
		datepicker(
			initialDate = initialDate,
			title = stringResource(titleRes),
			waitForPositiveButton = true,
			allowedDateValidator = allowedDateValidator,
			onDateChange = onDateChanged
		)
	}
}

