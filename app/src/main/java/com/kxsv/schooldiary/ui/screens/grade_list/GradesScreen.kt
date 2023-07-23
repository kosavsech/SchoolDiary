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
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.ui.main.topbar.GradesTopAppBar
import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.ui.GradesSortType
import com.kxsv.schooldiary.util.ui.LoadingContent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun GradesScreen(
	@StringRes userMessage: Int,
	onUserMessageDisplayed: () -> Unit,
	onGradeClick: (GradeEntity) -> Unit,
	openDrawer: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: GradesViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			GradesTopAppBar(
				openDrawer = openDrawer,
				onSortByMarkDate = { viewModel.sortGrades(GradesSortType.MARK_DATE) },
				onSortByFetchDate = { viewModel.sortGrades(GradesSortType.FETCH_DATE) }
			)
		},
		modifier = modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		GradesContent(
			loading = uiState.isLoading,
			grades = uiState.grades,
			//noSubjectsLabel = 0,
			onGradeClick = onGradeClick,
			onRefresh = { viewModel.fetchGrades() },
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
	grades: List<GradeWithSubject>,
	// TODO
	//  @StringRes noSubjectsLabel: Int,
	//  onRefresh: () -> Unit,
	onGradeClick: (GradeEntity) -> Unit,
	onRefresh: () -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		loading = loading,
		isContentScrollable = true,
		empty = grades.isEmpty(),
		emptyContent = { Text(text = "No subjects for yet") },
		onRefresh = onRefresh
	) {
		LazyColumn(
			modifier = modifier
				.padding(vertical = dimensionResource(R.dimen.list_item_padding)),
		) {
			items(grades) { grade ->
				GradeItem(
					gradeWithSubject = grade,
					onGradeClick = onGradeClick,
				)
			}
		}
	}
}

@Composable
private fun GradeItem(
	gradeWithSubject: GradeWithSubject,
	onGradeClick: (GradeEntity) -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onGradeClick(gradeWithSubject.grade) }
			.padding(
				horizontal = dimensionResource(R.dimen.horizontal_margin),
				vertical = dimensionResource(R.dimen.vertical_margin)
			)
	) {
		Text(
			text = gradeWithSubject.grade.mark.getValue(),
			style = MaterialTheme.typography.titleMedium,
		)
		Text(
			text = gradeWithSubject.subject.getName(),
			style = MaterialTheme.typography.titleMedium,
		)
		Text(
			text = gradeWithSubject.grade.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
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
				GradeWithSubject(
					GradeEntity(
						mark = Mark.FIVE,
						typeOfWork = "Самостоятельная работа",
						date = LocalDate.now(),
						fetchDateTime = LocalDateTime.now(),
						subjectMasterId = 0,
					),
					SubjectEntity("Английский язык")
				)
			),
			onGradeClick = {},
			onRefresh = {},
			modifier = Modifier
		)
	}
}

@Preview
@Composable
private fun SubjectItemPreview() {
	Surface {
		GradeItem(
			gradeWithSubject = GradeWithSubject(
				GradeEntity(
					mark = Mark.FIVE,
					typeOfWork = "Самостоятельная работа",
					date = LocalDate.now(),
					fetchDateTime = LocalDateTime.now(),
					subjectMasterId = 0,
				),
				SubjectEntity("Английский язык")
			),
			onGradeClick = {}
		)
	}
}