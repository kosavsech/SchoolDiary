package com.kxsv.schooldiary.ui.screens.grade_list

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.kxsv.schooldiary.ui.main.app_bars.topbar.GradesTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.GradesScreenNavActions
import com.kxsv.schooldiary.util.Mark
import com.kxsv.schooldiary.util.Mark.Companion.getStringValueFrom
import com.kxsv.schooldiary.util.ui.LoadingContent
import com.ramcosta.composedestinations.annotation.DeepLink
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.FULL_ROUTE_PLACEHOLDER
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val MY_URI = "https://www.school-diary.com"

@Destination(
	deepLinks = [
		DeepLink(
			action = Intent.ACTION_VIEW,
			uriPattern = "$MY_URI/$FULL_ROUTE_PLACEHOLDER"
		)
	]
)
@Composable
fun GradesScreen(
	destinationsNavigator: DestinationsNavigator,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	viewModel: GradesViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val navigator = GradesScreenNavActions(destinationsNavigator = destinationsNavigator)
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = {
			GradesTopAppBar(
				openDrawer = { coroutineScope.launch { drawerState.open() } },
				currentSortType = uiState.sortType,
				onSortChoose = { viewModel.sortGrades(it) },
			)
		},
		modifier = Modifier.fillMaxSize(),
	) { paddingValues ->
		
		GradesContent(
			loading = uiState.isLoading,
			grades = uiState.grades,
			onGradeClick = { gradeId -> navigator.onGradeClick(gradeId) },
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
		
		/*// Check if there's a userMessage to show to the user
		val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
		LaunchedEffect(userMessage) {
			if (userMessage != 0) {
				viewModel.showEditResultMessage(userMessage)
				currentOnUserMessageDisplayed()
			}
		}*/
	}
}

@Composable
private fun GradesContent(
	loading: Boolean,
	grades: List<GradeWithSubject>,
	// TODO
	//  @StringRes noSubjectsLabel: Int,
	//  onRefresh: () -> Unit,
	onGradeClick: (String) -> Unit,
	onRefresh: () -> Unit,
	modifier: Modifier,
) {
	LoadingContent(
		modifier = modifier,
		loading = loading,
		empty = grades.isEmpty(),
		emptyContent = { Text(text = "No subjects for yet") },
		isContentScrollable = true,
		onRefresh = onRefresh
	) {
		LazyColumn(
			contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.list_item_padding)),
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
	onGradeClick: (String) -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween,
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onGradeClick(gradeWithSubject.grade.gradeId) }
			.padding(
				horizontal = dimensionResource(R.dimen.horizontal_margin),
				vertical = dimensionResource(R.dimen.vertical_margin)
			)
	) {
		Text(
			text = getStringValueFrom(gradeWithSubject.grade.mark),
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