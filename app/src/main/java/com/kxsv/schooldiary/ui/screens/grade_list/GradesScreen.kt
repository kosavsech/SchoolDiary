package com.kxsv.schooldiary.ui.screens.grade_list

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.grade.Grade
import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.ui.GradesTopAppBar
import com.kxsv.schooldiary.util.ui.LoadingContent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GradesScreen(
	@StringRes userMessage: Int,
	onUserMessageDisplayed: () -> Unit,
	onAddGrade: () -> Unit,
	onGradeClick: (Grade) -> Unit,
	openDrawer: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: GradesViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { GradesTopAppBar(openDrawer = openDrawer) },
		modifier = modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(onClick = onAddGrade) {
				Icon(Icons.Default.Add, stringResource(R.string.add_grade))
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		GradesContent(
			loading = uiState.isLoading,
			grades = uiState.grades,
			//noSubjectsLabel = 0,
			onGradeClick = onGradeClick,
			modifier = Modifier.padding(paddingValues),
		)
		
		// Check for user messages to display on the screen
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
		
		// Check if there's a userMessage to show to the user
		val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
		LaunchedEffect(userMessage) {
			if (userMessage != 0) {
				viewModel.showEditResultMessage(userMessage)
				currentOnUserMessageDisplayed()
			}
		}
	}
}

@Composable
private fun GradesContent(
	loading: Boolean,
	grades: List<Grade>,
	// TODO
	//  @StringRes noSubjectsLabel: Int,
	//  onRefresh: () -> Unit,
	onGradeClick: (Grade) -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		loading = loading,
		isContentScrollable = true,
		empty = grades.isEmpty(),
		emptyContent = { Text(text = "No subjects for yet") },
		onRefresh = { /*TODO*/ }
	) {
		LazyColumn(
			modifier = modifier
				.padding(vertical = dimensionResource(R.dimen.list_item_padding)),
		) {
			items(grades) { grade ->
				GradeItem(
					grade = grade,
					onGradeClick = onGradeClick,
				)
			}
		}
	}
}

@Composable
private fun GradeItem(
	grade: Grade,
	onGradeClick: (Grade) -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onGradeClick(grade) }
			.padding(
				horizontal = dimensionResource(R.dimen.horizontal_margin),
				vertical = dimensionResource(R.dimen.vertical_margin)
			)
	) {
		Text(
			text = grade.mark.getValue(),
			style = MaterialTheme.typography.titleMedium,
		)
		Text(
			text = grade.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
			style = MaterialTheme.typography.titleMedium,
		)
		
	}
}


@Preview
@Composable
private fun SubjectsContentPreview() {
	Surface {
		GradesContent(
			loading = false,
			grades = listOf(
				Grade(
					mark = Mark.FIVE,
					date = LocalDate.now(),
					typeOfWork = "Самостоятельная работа",
					subjectMasterId = 0,
				)
			),
			onGradeClick = {},
			modifier = Modifier
		)
	}
}

@Preview
@Composable
private fun SubjectItemPreview() {
	Surface {
		GradeItem(grade = Grade(
			mark = Mark.FIVE,
			date = LocalDate.now(),
			typeOfWork = "Самостоятельная работа",
			subjectMasterId = 0,
		), onGradeClick = {})
	}
}