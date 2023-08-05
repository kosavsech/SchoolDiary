@file:OptIn(ExperimentalMaterial3Api::class)

package com.kxsv.schooldiary.ui.main.app_bars.topbar

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.util.ui.TasksDoneFilterType

@Composable
fun AddEditPatternTopAppBar(@StringRes title: Int, onBack: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = title)) },
		navigationIcon = {
			IconButton(onClick = onBack) {
				Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun AddEditScheduleTopAppBar(onBack: () -> Unit) {
	TopAppBar(
		title = {},
		navigationIcon = {
			IconButton(onClick = onBack) {
				Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun AddEditSubjectTopAppBar(onBack: () -> Unit) {
	TopAppBar(
		title = {},
		navigationIcon = {
			IconButton(onClick = onBack) {
				Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
			}
		},
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
		navigationIcon = {
			IconButton(onClick = onBack) {
				Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
			}
		},
		actions = {
			IconButton(
				onClick = fetchNet,
				enabled = fetchEnabled
			) {
				Icon(Icons.Filled.CloudDownload, stringResource(id = R.string.fetch_task))
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun AddEditGradeTopAppBar(onBack: () -> Unit) {
	TopAppBar(
		title = {},
		navigationIcon = {
			IconButton(onClick = onBack) {
				Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun PatternSelectionTopAppBar(onBack: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.pattern_selection_title)) },
		navigationIcon = {
			IconButton(onClick = onBack) {
				Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun PatternsTopAppBar(openDrawer: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.patterns_title)) },
		navigationIcon = {
			IconButton(onClick = openDrawer) {
				Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun TeachersTopAppBar(openDrawer: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.teachers_title)) },
		navigationIcon = {
			IconButton(onClick = openDrawer) {
				Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun SubjectsTopAppBar(openDrawer: () -> Unit) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.subjects_title)) },
		navigationIcon = {
			IconButton(onClick = openDrawer) {
				Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
			}
		},
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun TasksTopAppBar(
	openDrawer: () -> Unit,
	onDoneFilterSet: (TasksDoneFilterType) -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.agenda_title)) },
		navigationIcon = {
			IconButton(onClick = openDrawer) {
				Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
			}
		},
		actions = {
			var expanded by remember { mutableStateOf(false) }
			IconButton(onClick = { expanded = true }) {
				Icon(
					imageVector = Icons.Default.Sort,
					contentDescription = stringResource(R.string.grades_sort_type),
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
	onSortByMarkDate: () -> Unit,
	onSortByFetchDate: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.grades_title)) },
		navigationIcon = {
			IconButton(onClick = openDrawer) {
				Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
			}
		},
		actions = {
			GradesMoreActions(
				onSortByMarkDate = onSortByMarkDate,
				onSortByFetchDate = onSortByFetchDate
			)
		},
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun MainTopAppBar(
	openDrawer: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.main_menu_title)) },
		navigationIcon = {
			IconButton(onClick = openDrawer) {
				Icon(
					imageVector = Icons.Filled.Menu,
					contentDescription = stringResource(id = R.string.open_drawer)
				)
			}
		},
		modifier = Modifier.fillMaxWidth(),
	)
}

@Composable
fun EduPerformanceTopAppBar(
	openDrawer: () -> Unit,
) {
	TopAppBar(
		title = { Text(text = stringResource(id = R.string.report_card_title)) },
		navigationIcon = {
			IconButton(onClick = openDrawer) {
				Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
			}
		},
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
		navigationIcon = {
			IconButton(onClick = openDrawer) {
				Icon(
					imageVector = Icons.Filled.Menu,
					contentDescription = stringResource(id = R.string.open_drawer),
					tint = MaterialTheme.colorScheme.onSurface
				)
			}
		},
		// TODO: add action to switch screens of day/week view mode
		actions = {
			IconButton(onClick = onFetchSchedule) {
				Icon(
					imageVector = Icons.Filled.CloudDownload,
					contentDescription = stringResource(id = R.string.fetch_schedule),
					tint = MaterialTheme.colorScheme.onSurface
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

@Composable
private fun GradesMoreActions(
	onSortByMarkDate: () -> Unit,
	onSortByFetchDate: () -> Unit,
) {
	var expanded by remember { mutableStateOf(false) }
	IconButton(onClick = { expanded = true }) {
		Icon(
			imageVector = Icons.Default.Sort,
			contentDescription = stringResource(R.string.grades_sort_type),
		)
		DropdownMenu(
			expanded = expanded,
			onDismissRequest = { expanded = false },
		) {
			DropdownMenuItem(
				text = { Text(text = stringResource(R.string.mark_date_sort_type)) },
				onClick = { onSortByMarkDate(); expanded = false },
				leadingIcon = {
					Icon(
						Icons.Default.Event,
						stringResource(R.string.mark_date_sort),
					)
				}
			)
			DropdownMenuItem(
				text = { Text(text = stringResource(R.string.mark_fetch_date_sort_type)) },
				onClick = { onSortByFetchDate(); expanded = false },
				leadingIcon = {
					Icon(
						Icons.Default.Cached,
						stringResource(R.string.mark_fetch_date_sort),
					)
				}
			)
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
			tint = MaterialTheme.colorScheme.onSurface
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
						Icons.Default.Schedule,
						stringResource(R.string.change_pattern),
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
						tint = Color.Black
					)
				}
			)
			DropdownMenuItem(
				text = { Text(text = stringResource(R.string.copy_schedule_date_range)) },
				onClick = { onCopyDateRangeSchedule(); expanded.value = false },
				leadingIcon = {
					Icon(
						Icons.Default.DateRange,
						stringResource(R.string.copy_schedule_date_range),
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
		navigationIcon = {
			IconButton(onClick = onBack) {
				Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}

@Composable
fun SubjectDetailTopAppBar(title: String, onBack: () -> Unit, onDelete: () -> Unit) {
	TopAppBar(
		title = { Text(text = title) },
		navigationIcon = {
			IconButton(onClick = onBack) {
				Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.menu_back))
			}
		},
		actions = {
			IconButton(onClick = onDelete) {
				Icon(Icons.Filled.Delete, stringResource(id = R.string.menu_back))
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
					contentDescription = stringResource(id = R.string.task_topbar_close)
				)
			}
		},
		actions = {
			IconButton(onClick = onEdit) {
				Icon(Icons.Filled.Edit, stringResource(id = R.string.edit_task))
			}
			IconButton(onClick = onDelete) {
				Icon(Icons.Filled.Delete, stringResource(id = R.string.menu_back))
			}
		},
		modifier = Modifier.fillMaxWidth()
	)
}