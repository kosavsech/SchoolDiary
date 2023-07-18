package com.kxsv.schooldiary.ui.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R

@Composable
fun LoginScreen(
//	@StringRes userMessage: Int,
//	onUserMessageDisplayed: () -> Unit,
	onLogin: () -> Unit,
	modifier: Modifier = Modifier,
	viewModel: LoginViewModel = hiltViewModel(),
	snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
	Scaffold(
		snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
		modifier = modifier.fillMaxSize(),
	) { paddingValues ->
		val uiState = viewModel.uiState.collectAsState().value
		
		LoginContent(
			eduLogin = uiState.eduLogin,
			eduPassword = uiState.eduPassword,
			onLoginClick = { (viewModel::login)(); },
			eduLoginUpdate = viewModel::updateLogin,
			eduPasswordUpdate = viewModel::updatePassword,
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
		
		LaunchedEffect(uiState.loggedIn) {
			if (uiState.loggedIn) onLogin()
		}
		
		// Check if there's a userMessage to show to the user
		/*val currentOnUserMessageDisplayed by rememberUpdatedState(onUserMessageDisplayed)
		LaunchedEffect(userMessage) {
			if (userMessage != 0) {
				viewModel.processAuthError(userMessage)
				currentOnUserMessageDisplayed()
			}
		}*/
	}
}

@Composable
private fun LoginContent(
	eduLogin: String,
	eduPassword: String,
	eduLoginUpdate: (String) -> Unit,
	eduPasswordUpdate: (String) -> Unit,
	onLoginClick: () -> Unit,
	modifier: Modifier,
) {
	Box(
		modifier = Modifier.fillMaxSize(),
	) {
		ElevatedCard(Modifier.align(Center)) {
			Column(
				modifier = modifier.padding(dimensionResource(R.dimen.vertical_margin)),
			) {
				TextField(value = eduLogin, onValueChange = eduLoginUpdate)
				Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.list_item_padding)))
				TextField(value = eduPassword, onValueChange = eduPasswordUpdate)
				Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin)))
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


@Preview
@Composable
private fun SubjectsContentPreview() {
	Surface {
		LoginContent(
			eduLogin = "test",
			eduPassword = "pass",
			eduLoginUpdate = {},
			eduPasswordUpdate = {},
			onLoginClick = { },
			modifier = Modifier
		)
	}
}
