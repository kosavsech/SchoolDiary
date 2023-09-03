package com.kxsv.schooldiary.ui.screens.update_dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.pouyaheydari.appupdater.core.interactors.GetIsUpdateInProgress
import com.pouyaheydari.appupdater.core.pojo.DialogStates
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LoginViewModel"
private const val UNIQUE_WORK_NAME = "UpdateLoadWork"

data class UpdateUiState(
	val errorMessage: Int? = null,
	val dialogState: DialogStates = DialogStates.HideUpdateInProgress,
)

@HiltViewModel
class UpdateViewModel @Inject constructor(
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _uiState = MutableStateFlow(UpdateUiState())
	val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(errorMessage = null) }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(errorMessage = message) }
	}
	
	fun downloadApk() {
		observeUpdateInProgressStatus()
		_uiState.update { it.copy(dialogState = DialogStates.DownloadApk) }
	}
	
	private fun observeUpdateInProgressStatus() {
		viewModelScope.launch(ioDispatcher) {
			GetIsUpdateInProgress().invoke().collectLatest { isDownloading ->
				if (isDownloading) {
					_uiState.update { it.copy(dialogState = DialogStates.ShowUpdateInProgress) }
				} else {
					_uiState.update { it.copy(dialogState = DialogStates.HideUpdateInProgress) }
				}
			}
		}
	}
}
