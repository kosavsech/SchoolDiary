package com.kxsv.schooldiary.ui.screens.patterns

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternWithStrokes
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.TimePatternRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.ui.main.navigation.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.SELECTED_DEFAULT_PATTERN_OK
import com.kxsv.schooldiary.util.ui.Async
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
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

@HiltViewModel
class PatternsViewModel @Inject constructor(
	private val patternRepository: TimePatternRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	private val studyDayRepository: StudyDayRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val studyDayId: Long = savedStateHandle[AppDestinationsArgs.STUDY_DAY_ID_ARG]!!
	
	private val _patternsAsync =
		patternRepository.getPatternsWithStrokesStream()
			.map { Async.Success(it) }
			.catch<Async<List<TimePatternWithStrokes>>> { emit(Async.Error(R.string.loading_patterns_error)) }
	private val _defaultPatternId = userPreferencesRepository.observePatternId()
		.map { Async.Success(it) }
		.catch<Async<Long>> { emit(Async.Error(R.string.loading_default_pattern_id_error)) }
	
	private val _uiState = MutableStateFlow(PatternsUiState())
	val uiState: StateFlow<PatternsUiState> = combine(
		_uiState, _patternsAsync, _defaultPatternId
	) { state, patterns, defaultPatternId ->
		when (defaultPatternId) {
			is Async.Error -> {
				showSnackbarMessage(defaultPatternId.errorMessage)
			}
			
			Async.Loading -> {
				PatternsUiState(isLoading = true)
			}
			
			is Async.Success -> {
				loadDefaultPatternId(defaultPatternId.data)
			}
		}
		when (patterns) {
			Async.Loading -> {
				PatternsUiState(isLoading = true)
			}
			
			is Async.Error -> {
				PatternsUiState(
					userMessage = patterns.errorMessage,
					isPatternDeleted = state.isPatternDeleted
				)
			}
			
			is Async.Success -> {
				PatternsUiState(
					patterns = patterns.data,
					userMessage = state.userMessage,
					defaultPatternId = state.defaultPatternId,
					isLoading = state.isLoading,
					isPatternDeleted = state.isPatternDeleted
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, PatternsUiState())
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_pattern_message)
			ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_pattern_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_pattern_message)
			SELECTED_DEFAULT_PATTERN_OK -> showSnackbarMessage(R.string.successfully_set_default_pattern)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update {
			it.copy(userMessage = message)
		}
	}
	
	fun deletePattern(patternId: Long) = viewModelScope.launch {
		patternRepository.deletePattern(patternId)
		_uiState.update { it.copy(isPatternDeleted = true) }
	}
	
	fun updateDefaultPatternId(patternId: Long) = viewModelScope.launch {
		userPreferencesRepository.setPatternId(patternId)
		_uiState.update { it.copy(defaultPatternId = patternId) }
		showEditResultMessage(SELECTED_DEFAULT_PATTERN_OK)
	}
	
	private fun loadDefaultPatternId(patternId: Long) = viewModelScope.launch {
		_uiState.update { it.copy(defaultPatternId = patternId) }
	}
	
	fun selectCustomPattern(patternId: Long) {
		if (studyDayId == 0L) throw RuntimeException("selectCustomPattern() was called but no studyDayId is defined.")
		
		viewModelScope.launch {
			val studyDayToUpdate =
				studyDayRepository.getById(studyDayId)?.copy(appliedPatternId = patternId)
			if (studyDayToUpdate != null) {
				studyDayRepository.update(studyDayToUpdate)
			} else {
				throw NoSuchElementException("Not found StudyDayEntity(id = $studyDayId) to change its patternId.")
			}
		}
	}
}
