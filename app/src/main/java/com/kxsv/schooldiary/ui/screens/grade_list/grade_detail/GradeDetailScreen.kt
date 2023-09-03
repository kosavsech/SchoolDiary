package com.kxsv.schooldiary.ui.screens.grade_list.grade_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.ui.main.app_bars.topbar.AddEditGradeTopAppBar
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.GradeDetailScreenNavActions
import com.kxsv.schooldiary.ui.util.LoadingContent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class GradeDetailScreenNavArgs(
	val gradeId: String,
)

@Destination(
	navArgsDelegate = GradeDetailScreenNavArgs::class
)
@Composable
fun GradeDetailScreen(
	destinationsNavigator: DestinationsNavigator,
	viewModel: GradeDetailViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState,
) {
	val uiState = viewModel.uiState.collectAsState().value
	val navigator = GradeDetailScreenNavActions(destinationsNavigator = destinationsNavigator)
	Scaffold(
		modifier = Modifier.fillMaxSize(),
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		topBar = { AddEditGradeTopAppBar { navigator.popBackStack() } },
	) { paddingValues ->
		
		AddEditSubjectContent(
			modifier = Modifier.padding(paddingValues),
			isLoading = uiState.isLoading,
			mark = uiState.mark,
			typeOfWork = uiState.typeOfWork,
			gradeDate = uiState.gradeDate,
			pickedSubject = uiState.subject,
		)
		
		uiState.userMessage?.let { userMessage ->
			val snackbarText = stringResource(userMessage)
			LaunchedEffect(snackbarHostState, viewModel, userMessage, snackbarText) {
				snackbarHostState.showSnackbar(snackbarText)
				viewModel.snackbarMessageShown()
			}
		}
	}
}

@Composable
private fun AddEditSubjectContent(
	modifier: Modifier = Modifier,
	isLoading: Boolean,
	mark: String,
	typeOfWork: String,
	gradeDate: LocalDate?,
	pickedSubject: SubjectEntity?,
) {
	LoadingContent(
		loading = isLoading,
		empty = false,
		onRefresh = { /*TODO*/ }
	) {
		Column(
			modifier
				.fillMaxWidth()
				.padding(dimensionResource(id = R.dimen.horizontal_margin))
		) {
			MarkRow(mark = mark)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			TypeOfWorkRow(typeOfWork = typeOfWork)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			DateRow(date = gradeDate)
			
			Divider(
				Modifier
					.fillMaxWidth()
					.align(CenterHorizontally)
			)
			
			SubjectRow(subject = pickedSubject)
		}
	}
}

@Composable
private fun MarkRow(mark: String) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = dimensionResource(R.dimen.vertical_margin)),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Default.Favorite,
			contentDescription = stringResource(R.string.picked_date),
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = 8.dp))
		Text(
			text = mark,
			style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
		)
	}
}

@Composable
private fun TypeOfWorkRow(typeOfWork: String) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = dimensionResource(R.dimen.vertical_margin)),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Default.Description,
			contentDescription = stringResource(R.string.picked_date),
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = 8.dp))
		Text(
			text = typeOfWork,
			style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
		)
	}
}

@Composable
private fun DateRow(
	date: LocalDate?,
) {
	val pickDateText = date?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
		?: stringResource(R.string.pick_date_hint)
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = dimensionResource(R.dimen.vertical_margin)),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = Icons.Default.CalendarToday,
			contentDescription = stringResource(R.string.picked_date),
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = 8.dp))
		Text(
			text = pickDateText,
			style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
		)
	}
}


@Composable
private fun SubjectRow(
	subject: SubjectEntity?,
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = dimensionResource(R.dimen.vertical_margin)),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			Icons.Default.School,
			stringResource(R.string.picked_subject),
			Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.padding(horizontal = 8.dp))
		Text(
			text = subject?.getName() ?: stringResource(R.string.pick_subject_hint),
			style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
		)
	}
}
