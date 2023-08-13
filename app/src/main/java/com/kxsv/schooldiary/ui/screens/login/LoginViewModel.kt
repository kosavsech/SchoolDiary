package com.kxsv.schooldiary.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.util.NetworkError
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.di.util.IoDispatcher
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
	val errorMessage: Int? = null,
	val loggedIn: Boolean = false,
	val isLoading: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
	private val webService: WebService,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _uiState = MutableStateFlow(LoginUiState())
	val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(errorMessage = null) }
	}
	
	fun updatePassword(password: String) {
		_uiState.update { it.copy(eduPassword = password) }
	}
	
	fun updateLogin(login: String) {
		_uiState.update { it.copy(eduLogin = login) }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(errorMessage = message) }
	}
	
	private fun processAuthError(authError: NetworkError) {
		when (authError) {
			NetworkError.AccessTemporarilyBlocked -> showSnackbarMessage(R.string.access_temporarily_blocked)
			NetworkError.BlankInput -> showSnackbarMessage(R.string.blank_inupt)
			NetworkError.IncorrectAuthData -> showSnackbarMessage(R.string.incorrect_auth_data)
			NetworkError.NotLoggedIn -> showSnackbarMessage(R.string.not_logged_in)
			is NetworkError.GeneralError -> showSnackbarMessage(R.string.auth_failure)
			NetworkError.NotActualAuthSession -> showSnackbarMessage(R.string.auth_session_is_expired)
		}
	}
	
	fun onLoginClick() {
		if (uiState.value.eduLogin.isBlank() || uiState.value.eduPassword.isBlank()) {
			_uiState.update {
				it.copy(errorMessage = R.string.fill_required_fields_message)
			}
			return
		}
		_uiState.update { it.copy(errorMessage = null) }
		_uiState.update { it.copy(isLoading = true) }
		viewModelScope.launch(ioDispatcher) {
			try {
				webService.eduTatarAuth(
					login = uiState.value.eduLogin.trim(),
					password = uiState.value.eduPassword.trim()
				)
				_uiState.update { it.copy(isLoading = false) }
				_uiState.update { it.copy(loggedIn = true) }
			} catch (e: NetworkException) {
				_uiState.update { it.copy(isLoading = false) }
				processAuthError(authError = e.mapToNetworkError())
				Log.e(TAG, "onLoginClick: exception on onLoginClick", e)
			} catch (e: IOException) {
				Log.e(TAG, "onLoginClick: exception on connection execute", e)
			} catch (e: Exception) {
				Log.e(TAG, "onLoginClick: exception", e)
			}
		}
	}
}
