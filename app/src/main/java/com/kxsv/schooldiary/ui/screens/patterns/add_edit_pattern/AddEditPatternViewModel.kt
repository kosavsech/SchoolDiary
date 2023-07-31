package com.kxsv.schooldiary.ui.screens.patterns.add_edit_pattern

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.repository.PatternStrokeRepository
import com.kxsv.schooldiary.data.repository.TimePatternRepository
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.util.ListExtensionFunctions.copyExclusively
import com.kxsv.schooldiary.util.ListExtensionFunctions.copyInclusively
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class AddEditPatternUiState(
	val name: String = "",
	val strokes: MutableList<PatternStrokeEntity> = mutableListOf(),
	val startTime: LocalTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
	// TODO: configure this behaviour
	val endTime: LocalTime = startTime.plusMinutes(45),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val isStrokeDialogShown: Boolean = false,
	val stroke: PatternStrokeEntity? = null,
)

@HiltViewModel
class AddEditPatternViewModel @Inject constructor(
	private val patternRepository: TimePatternRepository,
	private val strokeRepository: PatternStrokeRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val navArgs: AddEditPatternScreenNavArgs = savedStateHandle.navArgs()
	private val patternId = navArgs.patternId
	
	private val _uiState = MutableStateFlow(AddEditPatternUiState())
	val uiState: StateFlow<AddEditPatternUiState> = _uiState.asStateFlow()
	
	init {
		if (patternId != null) loadPattern(patternId)
	}
	
	fun saveStroke() = viewModelScope.launch {
		val newStrokes: MutableList<PatternStrokeEntity>
		if (uiState.value.stroke != null) {
			newStrokes = copyExclusively(
				targetItem = uiState.value.stroke!!,
				elements = uiState.value.strokes
			)
			val updatedStroke = uiState.value.stroke!!.copy(
				startTime = uiState.value.startTime,
				endTime = uiState.value.endTime
			)
			newStrokes.add(updatedStroke)
		} else {
			val stroke =
				PatternStrokeEntity(
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
				startTime = it.endTime.plusMinutes(10),
				endTime = it.endTime.plusMinutes(55),
			)
		}
	}
	
	fun deleteStroke(stroke: PatternStrokeEntity) = viewModelScope.launch {
		if (stroke.strokeId != 0) strokeRepository.deleteStrokeById(stroke.strokeId)
		
		val newStrokes = copyExclusively(stroke, uiState.value.strokes)
		_uiState.update {
			it.copy(
				strokes = newStrokes
			)
		}
		showSnackbarMessage(R.string.successfully_deleted_stroke)
	}
	
	fun savePattern(): Int? {
		if (uiState.value.strokes.isEmpty()) {
			_uiState.update {
				it.copy(userMessage = R.string.empty_pattern_message)
			}
			return null
		}
		
		return if (patternId == null) {
			createNewPattern()
			ADD_RESULT_OK
		} else {
			updatePattern()
			EDIT_RESULT_OK
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
				startTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
				endTime = it.startTime.plusMinutes(45),
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
	
	fun onStrokeClick(stroke: PatternStrokeEntity) {
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
	}
	
	private fun updatePattern() {
		if (patternId == null) throw RuntimeException("updatePatternWithStrokes() was called but pattern is new.")
		
		viewModelScope.launch {
			patternRepository.updatePatternWithStrokes(
				TimePatternEntity(
					name = uiState.value.name,
					patternId = patternId,
				),
				strokes = uiState.value.strokes
			)
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
							strokes = timePattern.strokes as MutableList<PatternStrokeEntity>,
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