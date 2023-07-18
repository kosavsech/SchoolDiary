package com.kxsv.schooldiary.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.domain.ScheduleNetworkDataSource
import com.kxsv.schooldiary.util.NetworkError
import com.kxsv.schooldiary.util.NetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

private const val TAG = "LoginViewModel"

data class LoginUiState(
	val eduLogin: String = "",
	val eduPassword: String = "",
	val userMessage: Int? = null,
	val authError: NetworkError? = null,
	val loggedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
	private val networkDataSource: ScheduleNetworkDataSource,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _uiState = MutableStateFlow(LoginUiState())
	val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	fun updatePassword(password: String) {
		_uiState.update { it.copy(eduPassword = password) }
	}
	
	fun updateLogin(login: String) {
		_uiState.update { it.copy(eduLogin = login) }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	private fun processAuthError() {
		when (uiState.value.authError) {
			NetworkError.AccessTemporarilyBlocked -> showSnackbarMessage(R.string.access_temporarily_blocked)
			NetworkError.BlankInput -> showSnackbarMessage(R.string.blank_inupt)
			is NetworkError.GeneralError -> showSnackbarMessage(R.string.auth_failure)
			NetworkError.IncorrectAuthData -> showSnackbarMessage(R.string.incorrect_auth_data)
			NetworkError.NotLoggedIn -> showSnackbarMessage(R.string.not_logged_in)
			else -> Unit
		}
	}
	
	fun login() {
		if (uiState.value.eduLogin.isEmpty() || uiState.value.eduPassword.isEmpty()) {
			_uiState.update {
				it.copy(userMessage = R.string.fill_required_fields_message)
			}
			return
		}
		viewModelScope.launch(ioDispatcher) {
			try {
				networkDataSource.eduTatarAuth(uiState.value.eduLogin, uiState.value.eduPassword)
				_uiState.update { it.copy(loggedIn = true) }
			} catch (e: NetworkException) {
				_uiState.update { it.copy(authError = e.mapToNetworkError()) }
				processAuthError()
				Log.e(TAG, "login: exception on login", e)
			} catch (e: IOException) {
				Log.e(TAG, "login: exception on response parse", e)
			} catch (e: Exception) {
				Log.e(TAG, "login: exception", e)
			}
		}
	}
}