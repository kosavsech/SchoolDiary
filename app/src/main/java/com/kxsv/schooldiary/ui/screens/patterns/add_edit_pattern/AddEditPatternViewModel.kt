package com.kxsv.schooldiary.ui.screens.patterns.add_edit_pattern

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.AppDestinationsArgs
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.time_pattern.TimePattern
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.domain.PatternStrokeRepository
import com.kxsv.schooldiary.domain.TimePatternRepository
import com.kxsv.schooldiary.util.copyExclusively
import com.kxsv.schooldiary.util.copyInclusively
import com.kxsv.schooldiary.util.copyRefresh
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class AddEditPatternUiState(
	val name: String = "",
	val strokes: MutableList<PatternStroke> = mutableListOf(),
	val startTime: LocalTime = LocalTime.now(),
	// TODO: configure this behaviour
	val endTime: LocalTime = LocalTime.now().plusMinutes(45),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val isPatternSaved: Boolean = false,
	val isStrokeDialogShown: Boolean = false,
	val stroke: PatternStroke? = null,
)

@HiltViewModel
class AddEditPatternViewModel @Inject constructor(
	private val patternRepository: TimePatternRepository,
	private val strokeRepository: PatternStrokeRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val patternId: Long = savedStateHandle[AppDestinationsArgs.PATTERN_ID_ARG]!!
	
	private val _uiState = MutableStateFlow(AddEditPatternUiState())
	val uiState: StateFlow<AddEditPatternUiState> = _uiState.asStateFlow()
	
	init {
		if (patternId != 0L) loadPattern(patternId)
	}
	
	fun saveStroke() = viewModelScope.launch {
		val newStrokes: MutableList<PatternStroke>
		if (uiState.value.stroke != null) {
			newStrokes = copyRefresh(uiState.value.strokes)
			newStrokes.find { it == uiState.value.stroke }?.let {
				it.startTime = uiState.value.startTime
				it.endTime = uiState.value.endTime
			}
		} else {
			val stroke =
				PatternStroke(
					startTime = uiState.value.startTime,
					endTime = uiState.value.endTime
				)
			newStrokes = copyInclusively(stroke, uiState.value.strokes)
		}
		
		_uiState.update {
			it.copy(
				isStrokeDialogShown = false,
				stroke = null,
				strokes = newStrokes,
				startTime = LocalTime.MIDNIGHT,
				endTime = LocalTime.MIDNIGHT,
			)
		}
	}
	
	fun deleteStroke(stroke: PatternStroke) = viewModelScope.launch {
		if (stroke.strokeId != 0) strokeRepository.deleteStrokeById(stroke.strokeId)
		
		val newStrokes = copyExclusively(stroke, uiState.value.strokes)
		_uiState.update {
			it.copy(
				strokes = newStrokes
			)
		}
		showSnackbarMessage(R.string.successfully_deleted_stroke)
	}
	
	fun savePattern() {
		if (uiState.value.strokes.isEmpty()) {
			_uiState.update {
				it.copy(userMessage = R.string.empty_pattern_message)
			}
			return
		}
		
		if (patternId == 0L) {
			createNewPattern()
		} else {
			updatePattern()
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	fun hideStrokeDialog() {
		_uiState.update {
			it.copy(
				isStrokeDialogShown = false,
				startTime = LocalTime.MIDNIGHT,
				endTime = LocalTime.MIDNIGHT
			)
		}
	}
	
	fun showStrokeDialog() {
		_uiState.update {
			it.copy(
				isStrokeDialogShown = true,
			)
		}
		
	}
	
	fun updateEndTime(endTime: LocalTime) {
		_uiState.update {
			it.copy(
				endTime = endTime
			)
		}
	}
	
	fun updateStartTime(startTime: LocalTime) {
		_uiState.update {
			it.copy(
				startTime = startTime
			)
		}
	}
	
	fun updateName(name: String) {
		_uiState.update {
			it.copy(
				name = name
			)
		}
	}
	
	fun onStrokeClick(stroke: PatternStroke) {
		_uiState.update {
			it.copy(
				isStrokeDialogShown = true,
				startTime = stroke.startTime,
				endTime = stroke.endTime,
				stroke = stroke
			)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update {
			it.copy(userMessage = message)
		}
	}
	
	private fun createNewPattern() = viewModelScope.launch {
		patternRepository.createPatternWithStrokes(uiState.value.name, uiState.value.strokes)
		_uiState.update {
			it.copy(isPatternSaved = true)
		}
	}
	
	private fun updatePattern() {
		if (patternId == 0L) throw RuntimeException("updatePatternWithStrokes() was called but pattern is new.")
		
		viewModelScope.launch {
			patternRepository.updatePatternWithStrokes(
				TimePattern(
					name = uiState.value.name,
					patternId = patternId,
				),
				strokes = uiState.value.strokes
			)
			_uiState.update {
				it.copy(isPatternSaved = true)
			}
		}
	}
	
	private fun loadPattern(patternId: Long) {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch {
			patternRepository.getPatternWithStrokes(patternId).let { timePattern ->
				if (timePattern != null) {
					_uiState.update {
						it.copy(
							name = timePattern.timePattern.name,
							strokes = timePattern.strokes as MutableList<PatternStroke>,
							isLoading = false
						)
					}
				} else {
					_uiState.update {
						it.copy(isLoading = false)
					}
				}
			}
		}
	}
}