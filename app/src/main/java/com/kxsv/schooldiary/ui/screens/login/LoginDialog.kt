package com.kxsv.schooldiary.ui.screens.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.main.navigation.nav_actions.LoginDialogNavActions
import com.kxsv.schooldiary.ui.util.Indicator
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object NonDismissibleDialog : DestinationStyle.Dialog {
	override val properties = DialogProperties(
		dismissOnClickOutside = false,
		dismissOnBackPress = false,
	)
}

object DismissibleDialog : DestinationStyle.Dialog {
	override val properties = DialogProperties(
		dismissOnClickOutside = true,
		dismissOnBackPress = true,
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
	val navigator = LoginDialogNavActions(destinationsNavigator = destinationsNavigator)
	val uiState = viewModel.uiState.collectAsState().value
	val updateLogin = remember<(String) -> Unit> {
		{ viewModel.updateLogin(it) }
	}
	val updatePassword = remember<(String) -> Unit> {
		{ viewModel.updatePassword(it) }
	}
	val onLoginClick = remember {
		{ viewModel.onLoginClick() }
	}
	val onLoggedIn = remember {
		{ navigator.onLoggedIn() }
	}
	LaunchedEffect(uiState.loggedIn) {
		if (uiState.loggedIn) onLoggedIn()
	}
	LoginContent(
		isLoading = uiState.isLoading,
		eduLogin = uiState.eduLogin,
		eduPassword = uiState.eduPassword,
		errorMessage = uiState.errorMessage,
		eduLoginUpdate = updateLogin,
		eduPasswordUpdate = updatePassword,
		onLoginClick = onLoginClick,
	)
}

@Composable
private fun LoginContent(
	isLoading: Boolean,
	eduLogin: String,
	eduPassword: String,
	errorMessage: Int?,
	eduLoginUpdate: (String) -> Unit,
	eduPasswordUpdate: (String) -> Unit,
	onLoginClick: () -> Unit,
) {
	val coroutineScope = rememberCoroutineScope()
	Box(
		modifier = Modifier.fillMaxSize(),
	) {
		ElevatedCard(Modifier.align(Center)) {
			Column(
				modifier = Modifier.padding(dimensionResource(R.dimen.vertical_margin)),
			) {
				val focusManager = LocalFocusManager.current
				Text(
					text = stringResource(R.string.login_dialog_title),
					style = MaterialTheme.typography.headlineLarge,
					textAlign = TextAlign.Center
				)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
				TextField(
					value = eduLogin,
					onValueChange = eduLoginUpdate,
					label = { Text(text = stringResource(R.string.login_text_field_lable)) },
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Next,
						autoCorrect = false,
						capitalization = KeyboardCapitalization.None,
						keyboardType = KeyboardType.NumberPassword
					),
					keyboardActions = KeyboardActions(
						onNext = {
							focusManager.moveFocus(FocusDirection.Next)
						}
					),
					isError = errorMessage != null
				)
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
				var isPasswordVisible by remember { mutableStateOf(false) }
				TextField(
					value = eduPassword,
					onValueChange = eduPasswordUpdate,
					label = { Text(text = stringResource(R.string.password_text_field_lable)) },
					trailingIcon = {
						IconButton(onClick = {
							coroutineScope.coroutineContext.cancelChildren()
							if (isPasswordVisible) {
								isPasswordVisible = false
							} else {
								coroutineScope.launch {
									isPasswordVisible = true
									delay(2500)
									isPasswordVisible = false
								}
							}
						}) {
							Icon(
								imageVector = if (!isPasswordVisible) {
									Icons.Filled.Visibility
								} else {
									Icons.Filled.VisibilityOff
								},
								contentDescription = stringResource(R.string.toggle_password_visibility)
							)
						}
					},
					visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
					keyboardOptions = KeyboardOptions(
						imeAction = ImeAction.Done,
						autoCorrect = false,
						capitalization = KeyboardCapitalization.None,
						keyboardType = KeyboardType.Password
					),
					keyboardActions = KeyboardActions(
						onDone = {
							if (eduLogin.isNotEmpty()) {
								focusManager.clearFocus()
								onLoginClick()
							} else {
								focusManager.moveFocus(FocusDirection.Previous)
							}
						}
					),
					isError = errorMessage != null
				)
				if (errorMessage != null) {
					Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.list_item_padding)))
					Text(
						text = stringResource(id = errorMessage),
						color = MaterialTheme.colorScheme.error,
						style = MaterialTheme.typography.bodyLarge
					)
				}
				Spacer(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.vertical_margin)))
				Row(
					verticalAlignment = Alignment.CenterVertically
				) {
					Button(onClick = onLoginClick) {
						Text(
							text = stringResource(R.string.login_btn),
							style = MaterialTheme.typography.labelMedium
						)
					}
					if (isLoading) {
						Spacer(modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_item_padding)))
						Indicator(
							size = 32.dp,
						)
					}
				}
			}
		}
	}
}