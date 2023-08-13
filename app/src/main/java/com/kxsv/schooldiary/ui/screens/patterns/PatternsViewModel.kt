package com.kxsv.schooldiary.ui.screens.patterns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternWithStrokes
import com.kxsv.schooldiary.data.repository.TimePatternRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.SELECTED_DEFAULT_PATTERN_OK
import com.kxsv.schooldiary.ui.util.Async
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PatternsUiState(
	val patterns: List<TimePatternWithStrokes> = emptyList(),
	val defaultPatternId: Long = 0L,
	val userMessage: Int? = null,
	val isLoading: Boolean = false,
	val isPatternDeleted: Boolean = false,
)

private data class AsyncData(
	val patterns: List<TimePatternWithStrokes> = emptyList(),
	val defaultPatternId: Long = 0L,
)

@HiltViewModel
class PatternsViewModel @Inject constructor(
	private val patternRepository: TimePatternRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
	
	private val _patternsAsync = patternRepository.observePatternsWithStrokes()
	private val _defaultPatternId = userPreferencesRepository.observePatternId()
	
	private val _asyncData = combine(
		_patternsAsync, _defaultPatternId
	) { patterns, defaultPatternId ->
		AsyncData(patterns = patterns, defaultPatternId = defaultPatternId)
	}
		.map { Async.Success(it) }
		.catch<Async<AsyncData>> {
			emit(Async.Error(errorMessage = R.string.loading_patterns_error))
		}
		.stateIn(viewModelScope, WhileUiSubscribed, Async.Loading)
	
	private val _uiState = MutableStateFlow(PatternsUiState())
	val uiState: StateFlow<PatternsUiState> = combine(
		_uiState, _asyncData
	) { state, asyncData ->
		when (asyncData) {
			Async.Loading -> state.copy(isLoading = true)
			is Async.Error -> state.copy(userMessage = asyncData.errorMessage)
			is Async.Success -> state.copy(
				patterns = asyncData.data.patterns,
				defaultPatternId = asyncData.data.defaultPatternId
			)
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, PatternsUiState())
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_pattern_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_pattern_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_pattern_message)
			SELECTED_DEFAULT_PATTERN_OK -> showSnackbarMessage(R.string.successfully_set_default_pattern)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	fun deletePattern(patternId: Long) = viewModelScope.launch {
		patternRepository.deletePattern(patternId)
		showEditResultMessage(DELETE_RESULT_OK)
	}
	
	fun updateDefaultPatternId(patternId: Long) = viewModelScope.launch {
		userPreferencesRepository.setPatternId(patternId)
//		_uiState.update { it.copy(defaultPatternId = patternId) }
		showEditResultMessage(SELECTED_DEFAULT_PATTERN_OK)
	}
	
}
