package com.kxsv.schooldiary.ui.screens.subject_list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.util.AppVersionState
import com.kxsv.schooldiary.ui.main.app_bars.topbar.SubjectsTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.AppUpdateNavActions
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.SubjectsScreenNavActions
import com.kxsv.schooldiary.ui.screens.destinations.AddEditSubjectScreenDestination
import com.kxsv.schooldiary.ui.util.AppSnackbarHost
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.result.NavResult
import com.ramcosta.composedestinations.result.ResultRecipient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Destination
@Composable
fun SubjectsScreen(
	destinationsNavigator: DestinationsNavigator,
	subjectAddEditResult: ResultRecipient<AddEditSubjectScreenDestination, Int>,
	drawerState: DrawerState,
	coroutineScope: CoroutineScope,
	viewModel: SubjectsViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val navigator = SubjectsScreenNavActions(destinationsNavigator = destinationsNavigator)
	val updateDialog = AppUpdateNavActions(destinationsNavigator = destinationsNavigator)
	val toShowUpdateDialog = viewModel.toShowUpdateDialog.collectAsState().value
	LaunchedEffect(toShowUpdateDialog) {
		when (toShowUpdateDialog) {
			is AppVersionState.MustUpdate -> {
				updateDialog.onMandatoryUpdate(toShowUpdateDialog.update)
			}
			
			is AppVersionState.ShouldUpdate -> {
				updateDialog.onAvailableUpdate(toShowUpdateDialog.update)
				viewModel.onUpdateDialogShown()
			}
			
			else -> Unit
		}
	}
	subjectAddEditResult.onNavResult { result ->
		when (result) {
			is NavResult.Canceled -> {}
			is NavResult.Value -> {
				viewModel.showEditResultMessage(result.value)
			}
		}
	}
	Scaffold(
		snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
		topBar = { SubjectsTopAppBar(openDrawer = { coroutineScope.launch { drawerState.open() } }) },
		modifier = Modifier.fillMaxSize(),
		floatingActionButton = {
			FloatingActionButton(onClick = { navigator.onAddSubject() }) {
				Icon(Icons.Default.Add, stringResource(R.string.add_subject))
			}
		}
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		val onRefresh = remember {
			{ viewModel.refresh() }
		}
		SubjectsContent(
			modifier = Modifier.padding(paddingValues),
			loading = uiState.isLoading,
			subjects = uiState.subjects,
			onSubjectClick = { subjectId -> navigator.onSubjectClick(subjectId) },
			onRefresh = onRefresh,
		)
		
		// Check for user messages to display on the screen
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
	}
}

@Composable
private fun SubjectsContent(
	modifier: Modifier,
	loading: Boolean,
	subjects: List<SubjectEntity>,
	onSubjectClick: (String) -> Unit,
	onRefresh: () -> Unit,
) {
	LoadingContent(
		modifier = modifier,
		isLoading = loading,
		empty = subjects.isEmpty(),
		emptyContent = { Text(text = "No subjects yet") },
		isContentScrollable = true,
		onRefresh = onRefresh
	) {
		LazyColumn(
			contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.list_item_padding)),
		) {
			items(subjects) { subject ->
				SubjectItem(
					subject = subject,
					onSubjectClick = onSubjectClick,
				)
				Divider()
			}
		}
	}
}

@Composable
private fun SubjectItem(
	subject: SubjectEntity,
	onSubjectClick: (String) -> Unit,
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = Modifier
			.fillMaxWidth()
			.clickable { onSubjectClick(subject.subjectId) }
			.padding(
				horizontal = dimensionResource(R.dimen.horizontal_margin),
				vertical = dimensionResource(R.dimen.vertical_margin)
			)
	) {
		Text(
			text = subject.getName(),
			style = MaterialTheme.typography.titleMedium,
		)
	}
	
}

@Preview
@Composable
private fun SubjectsContentPreview() {
	Surface {
		SubjectsContent(
			modifier = Modifier,
			loading = false,
			subjects = listOf(
				SubjectEntity("Algebra", cabinet = "52"),
				SubjectEntity("Algebra", cabinet = "48"),
				SubjectEntity("Algebra", cabinet = "62"),
				SubjectEntity("Algebra", cabinet = "75"),
			),
			onSubjectClick = {},
			onRefresh = {}
		)
	}
}

@Preview
@Composable
private fun SubjectItemPreview() {
	Surface {
		SubjectItem(subject = SubjectEntity("Algebra", cabinet = "850"), onSubjectClick = {})
	}
}