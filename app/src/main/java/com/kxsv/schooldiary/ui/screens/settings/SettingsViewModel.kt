package com.kxsv.schooldiary.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.repository.UpdateRepository
import com.kxsv.schooldiary.data.util.AppVersionState
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
	val userMessage: Int? = null,
	val userMessageArgs: Array<out Any>? = null,
	val isLoading: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
	private val updateRepository: UpdateRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _uiState = MutableStateFlow(SettingsUiState())
	val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
	
	val toShowUpdateDialog = MutableStateFlow<AppVersionState>(AppVersionState.NotFound)
	
	private fun observeIsUpdateAvailable() {
		viewModelScope.launch(ioDispatcher) {
			updateRepository.isUpdateAvailable.collect {
				toShowUpdateDialog.value = it
			}
		}
	}
	
	fun onUpdateDialogShown() {
		viewModelScope.launch(ioDispatcher) {
			toShowUpdateDialog.value = AppVersionState.Suppressed
			updateRepository.suppressUpdateUntilNextAppStart()
		}
	}
	
	init {
		observeIsUpdateAvailable()
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null, userMessageArgs = null)
		}
	}
	
	fun nonFunctionalCategoryClicked() {
		showSnackbarMessage(R.string.non_functional_category)
	}
	
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	private fun setSnackbarArgs(vararg args: Any) {
		_uiState.update { it.copy(userMessageArgs = args) }
	}
	
}