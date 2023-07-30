package com.kxsv.schooldiary.ui.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.screens.destinations.DayScheduleScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle

object NonDismissibleDialog : DestinationStyle.Dialog {
	override val properties = DialogProperties(
		dismissOnClickOutside = false,
		dismissOnBackPress = false,
	)
}

private const val TAG = "LoginDialog"

@LoginNavGraph
@Destination(
	style = NonDismissibleDialog::class,
)
@Composable
fun LoginDialog(
	destinationsNavigator: DestinationsNavigator,
	viewModel: LoginViewModel = hiltViewModel(),
) {
	val uiState = viewModel.uiState.collectAsState().value
	LoginContent(
		eduLogin = uiState.eduLogin,
		eduPassword = uiState.eduPassword,
		isLoggedIn = uiState.loggedIn,
		errorMessage = uiState.errorMessage,
		eduLoginUpdate = { viewModel.updateLogin(it) },
		eduPasswordUpdate = { viewModel.updatePassword(it) },
		onLoginClick = { viewModel.login() },
		onLoggedIn = {
			destinationsNavigator.navigate(DayScheduleScreenDestination.route)
		}
	)
}

@Composable
private fun LoginContent(
	eduLogin: String,
	eduPassword: String,
	isLoggedIn: Boolean,
	errorMessage: Int?,
	eduLoginUpdate: (String) -> Unit,
	eduPasswordUpdate: (String) -> Unit,
	onLoginClick: () -> Unit,
	onLoggedIn: () -> Unit,
) {
	Box(
		modifier = Modifier.fillMaxSize(),
	) {
		LaunchedEffect(isLoggedIn) {
			if (isLoggedIn) onLoggedIn()
		}
		ElevatedCard(Modifier.align(Center)) {
			Column(
				modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin)),
			) {
				TextField(value = eduLogin, onValueChange = eduLoginUpdate)
				Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.list_item_padding)))
				TextField(value = eduPassword, onValueChange = eduPasswordUpdate)
				Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin)))
				if (errorMessage != null) {
					Text(text = stringResource(id = errorMessage))
				}
				Button(onClick = onLoginClick) {
					Text(
						text = "Login",
						style = MaterialTheme.typography.labelMedium
					)
				}
			}
		}
	}
}