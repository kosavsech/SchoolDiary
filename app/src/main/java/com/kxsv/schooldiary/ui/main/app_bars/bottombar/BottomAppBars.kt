package com.kxsv.schooldiary.ui.main.app_bars.bottombar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.util.TasksDateFilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksBottomAppBar(
	selectedDataFilterText: String,
	onAddTask: () -> Unit,
	onDateFilterChoose: (TasksDateFilterType) -> Unit,
) {
	var expanded by remember { mutableStateOf(false) }
	BottomAppBar(
		modifier = Modifier
			.clickable { expanded = !expanded }
			.fillMaxWidth(),
	) {
		val dataFilterOptions = TasksDateFilterType.values()
		Row {
			DropdownMenu(
				expanded = expanded,
				onDismissRequest = { expanded = false },
				offset = DpOffset(50.dp, 0.dp),
			) {
				dataFilterOptions.forEach { filterOption ->
					DropdownMenuItem(
						text = {
							Text(
								text = stringResource(
									id = filterOption.getLocalisedStringId(),
									"Specific date"
								)
							)
						},
						onClick = {
							onDateFilterChoose(filterOption)
							expanded = false
						},
						modifier = Modifier.width(200.dp)
					)
				}
			}
			Column(
				modifier = Modifier
					.padding(
						vertical = dimensionResource(R.dimen.vertical_margin),
						horizontal = 24.dp,
					)
			) {
				Text(
					text = stringResource(R.string.date_filter),
					color = MaterialTheme.colorScheme.onSurface,
					style = MaterialTheme.typography.bodyLarge,
				)
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					Icon(
						imageVector = Icons.Default.CalendarMonth,
						contentDescription = stringResource(R.string.due_date_filter),
					)
					Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
					Text(
						text = selectedDataFilterText,
						style = MaterialTheme.typography.titleLarge,
					)
				}
			}
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

