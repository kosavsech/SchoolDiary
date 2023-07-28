package com.kxsv.schooldiary.ui.main.app_bars.bottombar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.util.ui.TasksDateFilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksBottomAppBar(
	selectedDataFilterText: String,
	onAddTask: () -> Unit,
	onDateFilterChoose: (TasksDateFilterType) -> Unit,
) {
	BottomAppBar(
		modifier = Modifier.fillMaxWidth(),
	) {
		var expanded by remember { mutableStateOf(false) }
		val dataFilterOptions = TasksDateFilterType.values()
		Row() {
			ExposedDropdownMenuBox(
				expanded = expanded,
				onExpandedChange = { expanded = it }
			) {
				ExposedDropdownMenu(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					dataFilterOptions.forEach { filterOption ->
						DropdownMenuItem(
							text = {
								Text(text = stringResource(id = filterOption.getLocalisedStringId()))
							},
							onClick = {
								onDateFilterChoose(filterOption)
								expanded = false
							}
						)
					}
				}
				TextField(
					readOnly = true,
					value = selectedDataFilterText,
					onValueChange = {},
					label = {
						Text(
							text = stringResource(R.string.date_filter),
							style = MaterialTheme.typography.bodyMedium,
						)
					},
					leadingIcon = {
						Icon(
							imageVector = Icons.Default.CalendarMonth,
							contentDescription = stringResource(R.string.due_date_filter)
						)
					},
					modifier = Modifier.menuAnchor()
				)
			}
			Spacer(Modifier.weight(1f, true))
			Box(
				Modifier
					.fillMaxHeight()
					.padding(
						top = 8.dp,
						end = 12.dp
					),
				contentAlignment = Alignment.TopStart
			) {
				FloatingActionButton(onClick = onAddTask) {
					Icon(Icons.Default.Add, stringResource(R.string.add_task))
				}
			}
		}
	}
}

