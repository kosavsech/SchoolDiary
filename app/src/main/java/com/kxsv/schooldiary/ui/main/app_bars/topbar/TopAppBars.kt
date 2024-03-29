@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.kxsv.schooldiary.ui.main.app_bars.topbar

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.EduPerformanceScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.GradesScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TasksScreenDestination
import com.kxsv.schooldiary.ui.screens.destinations.TypedDestination
import com.kxsv.schooldiary.ui.util.GradesSortType
import com.kxsv.schooldiary.ui.util.TasksDoneFilterType

@Composable
fun AddEditPatternTopAppBar(@StringRes title: Int, onBack: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = title)) },
		navigationIcon = { BackIconButton(onBack) },
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun AddEditScheduleTopAppBar(onBack: () -> Unit) {
	TopAppBar(
		title = {},
		navigationIcon = { BackIconButton(onBack) },
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun AddEditSubjectTopAppBar(onBack: () -> Unit) {
	TopAppBar(
		title = {},
		navigationIcon = { BackIconButton(onBack) },
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun AddEditTaskTopAppBar(
	onBack: () -> Unit,
	fetchNet: () -> Unit,
	fetchEnabled: Boolean,
) {
	TopAppBar(
		title = {},
		navigationIcon = { BackIconButton(onBack) },
		actions = {
			IconButton(
				onClick = fetchNet,
				enabled = fetchEnabled
			) {
				Icon(
					imageVector = Icons.Filled.CloudDownload,
					contentDescription = stringResource(id = R.string.fetch_task),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun AddEditGradeTopAppBar(onBack: () -> Unit) {
	TopAppBar(
		title = {},
		navigationIcon = { BackIconButton(onBack) },
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun PatternSelectionTopAppBar(onBack: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.pattern_selection_title)) },
		navigationIcon = { BackIconButton(onBack) },
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun PatternsTopAppBar(openDrawer: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.patterns_title)) },
		navigationIcon = { DrawerIconButton(openDrawer) },
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun TeachersTopAppBar(openDrawer: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.teachers_title)) },
		navigationIcon = { DrawerIconButton(openDrawer) },
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun SubjectsTopAppBar(openDrawer: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.subjects_title)) },
		navigationIcon = { DrawerIconButton(openDrawer) },
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
private fun BackIconButton(
	onBack: () -> Unit,
) {
	IconButton(onClick = onBack) {
		Icon(
			imageVector = Icons.Filled.ArrowBack,
			contentDescription = stringResource(id = R.string.menu_back),
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
private fun DrawerIconButton(
	openDrawer: () -> Unit,
) {
	IconButton(onClick = openDrawer) {
		Icon(
			imageVector = Icons.Filled.Menu,
			contentDescription = stringResource(id = R.string.open_drawer),
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
fun TasksTopAppBar(
	openDrawer: () -> Unit,
	onDoneFilterSet: (TasksDoneFilterType) -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.agenda_title)) },
		navigationIcon = { DrawerIconButton(openDrawer) },
		actions = {
			var expanded by remember { mutableStateOf(false) }
			IconButton(onClick = { expanded = true }) {
				Icon(
					imageVector = Icons.Default.Sort,
					contentDescription = stringResource(R.string.grades_sort_type),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
				DropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false },
				) {
					DropdownMenuItem(
						text = { Text(text = stringResource(R.string.all_filter)) },
						onClick = { onDoneFilterSet(TasksDoneFilterType.ALL); expanded = false },
					)
					DropdownMenuItem(
						text = { Text(text = stringResource(R.string.hide_done_tasks_filter)) },
						onClick = {
							onDoneFilterSet(TasksDoneFilterType.IS_NOT_DONE); expanded = false
						},
					)
				}
			}
		},
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun GradesTopAppBar(
	openDrawer: () -> Unit,
	currentSortType: GradesSortType,
	onSortChoose: (GradesSortType) -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.grades_title)) },
		navigationIcon = { DrawerIconButton(openDrawer) },
		actions = {
			GradesMoreActions(
				currentSortType = currentSortType,
				onSortChoose = onSortChoose,
			)
		},
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun MainTopAppBar(
	onNavigate: (String) -> Unit,
	openDrawer: () -> Unit,
) {
	Column {
		TopAppBar(
			title = { Text(text = stringResource(id = R.string.main_menu_title)) },
			navigationIcon = { DrawerIconButton(openDrawer) },
			modifier = Modifier.fillMaxWidth(),
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
			)
		)
		MainScreenChipSection(onNavigate)
	}
}

@Composable
private fun MainScreenChipSection(
	onNavigate: (String) -> Unit,
) {
	data class NavButton(
		@StringRes val res: Int,
		val destination: TypedDestination<out Any>,
	)
	
	val buttons = listOf(
		NavButton(res = R.string.timetable, destination = DayScheduleScreenDestination),
		NavButton(res = R.string.tasks_title, destination = TasksScreenDestination),
		NavButton(res = R.string.grades_title, destination = GradesScreenDestination),
		NavButton(res = R.string.report_card_title, destination = EduPerformanceScreenDestination),
	)
	
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.horizontalScroll(rememberScrollState())
			.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
//			.background(MaterialTheme.colorScheme.background)
			.padding(bottom = dimensionResource(R.dimen.vertical_margin)),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		buttons.forEach {
			key(it.res) {
				OutlinedButton(
					onClick = { onNavigate(it.destination.route) },
					modifier = Modifier.padding(
						horizontal = dimensionResource(R.dimen.list_item_padding)
					),
				) {
					Text(
						text = stringResource(it.res),
						style = MaterialTheme.typography.labelMedium
					)
				}
			}
		}
	}
}

@Composable
fun EduPerformanceTopAppBar(
	openDrawer: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.report_card_title)) },
		navigationIcon = { DrawerIconButton(openDrawer) },
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun SettingsTopAppBar(
	openDrawer: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.settings_title)) },
		navigationIcon = { DrawerIconButton(openDrawer) },
		modifier = Modifier.fillMaxWidth(),
	)
}


@Composable
fun GeneralSettingsTopAppBar(
	onBack: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.general_settings)) },
		navigationIcon = { BackIconButton(onBack = onBack) },
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun GradeSettingsTopAppBar(
	onBack: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.grade_settings)) },
		navigationIcon = { BackIconButton(onBack = onBack) },
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun TermsSettingsTopAppBar(
	onBack: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.terms_settings)) },
		navigationIcon = { BackIconButton(onBack = onBack) },
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun TimetableSettingsTopAppBar(
	onBack: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.timetable_settings)) },
		navigationIcon = { BackIconButton(onBack = onBack) },
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun ScheduleTopAppBar(
	onChangePattern: () -> Unit,
	onCopyDaySchedule: () -> Unit,
	onCopyDateRangeSchedule: () -> Unit,
	onFetchSchedule: () -> Unit,
	openDrawer: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(R.string.timetable)) },
		navigationIcon = { DrawerIconButton(openDrawer) },
		// TODO: add action to switch screens of day/week view mode
		actions = {
			IconButton(onClick = onFetchSchedule) {
				Icon(
					imageVector = Icons.Filled.CloudDownload,
					contentDescription = stringResource(id = R.string.fetch_schedule),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			ScheduleMoreActions(
				onChangePattern = onChangePattern,
				onCopyDaySchedule = onCopyDaySchedule,
				onCopyDateRangeSchedule = onCopyDateRangeSchedule,
			)
		},
		modifier = Modifier.fillMaxWidth()
	)
	
}

enum class GradeSortTypeItem(
	val sortType: GradesSortType,
	val icon: ImageVector,
	@StringRes val label: Int,
) {
	MarkDate(
		sortType = GradesSortType.MARK_DATE,
		icon = Icons.Default.Event,
		label = R.string.mark_date_sort_type
	),
	FetchDate(
		sortType = GradesSortType.FETCH_DATE,
		icon = Icons.Default.Cached,
		label = R.string.mark_fetch_date_sort_type
	)
}

@Composable
private fun GradesMoreActions(
	currentSortType: GradesSortType,
	onSortChoose: (GradesSortType) -> Unit,
) {
	var expanded by remember { mutableStateOf(false) }
	IconButton(onClick = { expanded = true }) {
		Icon(
			imageVector = Icons.Default.Sort,
			contentDescription = stringResource(R.string.grades_sort_type),
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false },
		) {
			GradeSortTypeItem.values().forEach {
				val isSelected = currentSortType == it.sortType
				val backgroundModifier = if (isSelected) {
					Modifier.background(MaterialTheme.colorScheme.outlineVariant)
				} else Modifier
				DropdownMenuItem(
					text = { Text(text = stringResource(it.label)) },
					onClick = { onSortChoose(it.sortType); expanded = false },
					enabled = !isSelected,
					modifier = Modifier.then(backgroundModifier),
					leadingIcon = {
						Icon(
							imageVector = it.icon,
							contentDescription = "",
							tint = MaterialTheme.colorScheme.onSurfaceVariant
						)
					},
					colors = MenuDefaults.itemColors(
						textColor = MaterialTheme.colorScheme.onSurface,
						disabledTextColor = MaterialTheme.colorScheme.onSurface,
						leadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
						disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
					)
				)
			}
		}
	}
}

@Composable
private fun ScheduleMoreActions(
	onChangePattern: () -> Unit,
	onCopyDaySchedule: () -> Unit,
	onCopyDateRangeSchedule: () -> Unit,
) {
	val expanded = remember { mutableStateOf(false) }
	IconButton(onClick = { expanded.value = true }) {
		Icon(
			imageVector = Icons.Default.MoreVert,
			contentDescription = stringResource(R.string.more_actions),
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
		DropdownMenu(
			expanded = expanded.value,
			onDismissRequest = { expanded.value = false },
		) {
			DropdownMenuItem(
				text = { Text(text = stringResource(R.string.change_pattern)) },
				onClick = { onChangePattern(); expanded.value = false },
				leadingIcon = {
					Icon(
						imageVector = Icons.Default.Schedule,
						contentDescription = stringResource(R.string.change_pattern),
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			)
			DropdownMenuItem(
				text = { Text(text = stringResource(R.string.copy_schedule_day)) },
				onClick = { onCopyDaySchedule(); expanded.value = false },
				leadingIcon = {
					Icon(
						Icons.Default.Today,
						stringResource(R.string.copy_schedule_day),
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			)
			DropdownMenuItem(
				text = { Text(text = stringResource(R.string.copy_schedule_date_range)) },
				onClick = { onCopyDateRangeSchedule(); expanded.value = false },
				leadingIcon = {
					Icon(
						imageVector = Icons.Default.DateRange,
						contentDescription = stringResource(R.string.copy_schedule_date_range),
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			)
		}
	}
}

@Composable
fun CopyScheduleForDayTopAppBar(date: String? = "", onBack: () -> Unit) {
	TopAppBar(
		title = { if (date != null) Text(text = "Copy lesson to $date") },
		navigationIcon = { BackIconButton(onBack) },
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun SubjectDetailTopAppBar(
	title: String,
	onBack: () -> Unit,
	onDelete: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = title) },
		navigationIcon = { BackIconButton(onBack) },
		actions = {
			IconButton(onClick = onDelete) {
				Icon(
					imageVector = Icons.Filled.Delete,
					contentDescription = stringResource(id = R.string.menu_back),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun TaskDetailTopAppBar(onBack: () -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
	TopAppBar(
		title = {},
		navigationIcon = {
			IconButton(onClick = onBack) {
				Icon(
					imageVector = Icons.Filled.Close,
					contentDescription = stringResource(id = R.string.task_topbar_close),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		},
		actions = {
			IconButton(onClick = onEdit) {
				Icon(
					imageVector = Icons.Filled.Edit,
					contentDescription = stringResource(id = R.string.edit_task),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			IconButton(onClick = onDelete) {
				Icon(
					imageVector = Icons.Filled.Delete,
					contentDescription = stringResource(id = R.string.menu_back),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}