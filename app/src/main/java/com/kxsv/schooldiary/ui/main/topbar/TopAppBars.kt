package com.kxsv.schooldiary.ui.main.topbar

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.kxsv.schooldiary.R

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
				Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
			}
		},
		// TODO: add action to switch screens of day/week view mode
		actions = {
			IconButton(onClick = onFetchSchedule) {
				Icon(Icons.Filled.CloudDownload, stringResource(id = R.string.fetch_schedule))
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
	val expanded = remember { mutableStateOf(false) }
	IconButton(onClick = { expanded.value = true }) {
		Icon(
			imageVector = Icons.Default.Sort,
			contentDescription = stringResource(R.string.grades_sort_type),
			tint = Color.White
		)
		DropdownMenu(
			expanded = expanded.value,
			onDismissRequest = { expanded.value = false },
		) {
			DropdownMenuItem(
				text = { Text(text = stringResource(R.string.mark_date_sort_type)) },
				onClick = { onSortByMarkDate(); expanded.value = false },
				leadingIcon = {
					Icon(
						Icons.Default.Event,
						stringResource(R.string.mark_date_sort),
						tint = Color.Black
					)
				}
			)
			DropdownMenuItem(
				text = { Text(text = stringResource(R.string.mark_fetch_date_sort_type)) },
				onClick = { onSortByFetchDate(); expanded.value = false },
				leadingIcon = {
					Icon(
						Icons.Default.Cached,
						stringResource(R.string.mark_fetch_date_sort),
						tint = Color.Black
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
			tint = Color.White
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
						tint = Color.Black
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
						tint = Color.Black
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
fun SubjectDetailTopAppBar(title: String? = "", onBack: () -> Unit, onDelete: () -> Unit) {
	TopAppBar(
		title = { if (title != null) Text(text = title) },
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