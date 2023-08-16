package com.kxsv.schooldiary.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.app.sync.initializers.SyncConstraints
import com.kxsv.schooldiary.app.sync.workers.DelegatingWorker
import com.kxsv.schooldiary.app.sync.workers.GradeSyncWorker
import com.kxsv.schooldiary.app.sync.workers.ScheduleSyncWorker
import com.kxsv.schooldiary.app.sync.workers.SubjectsSyncWorker
import com.kxsv.schooldiary.app.sync.workers.TaskSyncWorker
import com.kxsv.schooldiary.app.sync.workers.delegatedData
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.remote.util.NetworkError
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
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
private const val UNIQUE_WORK_NAME = "OnLoggedInFetchSync"

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
	private val workManager: WorkManager,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
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
	
	fun onLoggedIn() {
		val subjectsSyncRequest =
			OneTimeWorkRequestBuilder<DelegatingWorker>()
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setConstraints(SyncConstraints)
				.setInputData(SubjectsSyncWorker::class.delegatedData())
				.build()
		
		val scheduleSyncRequest =
			OneTimeWorkRequestBuilder<DelegatingWorker>()
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setConstraints(SyncConstraints)
				.setInputData(ScheduleSyncWorker::class.delegatedData())
				.build()
		
		
		val tasksSyncRequest =
			OneTimeWorkRequestBuilder<DelegatingWorker>()
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setConstraints(SyncConstraints)
				.setInputData(TaskSyncWorker::class.delegatedData())
				.build()
		
		val gradesSyncRequest =
			OneTimeWorkRequestBuilder<DelegatingWorker>()
				.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
				.setConstraints(SyncConstraints)
				.setInputData(GradeSyncWorker::class.delegatedData())
				.build()
		
		val continuation = workManager
			.beginUniqueWork(
				UNIQUE_WORK_NAME,
				ExistingWorkPolicy.KEEP,
				subjectsSyncRequest
			)
			.then(listOf(scheduleSyncRequest, tasksSyncRequest, gradesSyncRequest))
		
		continuation.enqueue()
	}
}
