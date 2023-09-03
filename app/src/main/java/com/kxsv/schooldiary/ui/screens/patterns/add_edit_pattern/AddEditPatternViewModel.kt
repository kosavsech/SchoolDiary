package com.kxsv.schooldiary.ui.screens.patterns.add_edit_pattern

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.TimePatternEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.repository.TimePatternRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.util.ListExtensionFunctions.copyExclusively
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
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
	val strokes: List<PatternStrokeEntity> = mutableListOf(),
	
	val startTime: LocalTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
	val endTime: LocalTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
	val index: Int = 1,
	
	val strokeToUpdate: PatternStrokeEntity? = null,
	
	val isLoading: Boolean = false,
	val errorMessage: Int? = null,
	val userMessage: Int? = null,
)

@HiltViewModel
class AddEditPatternViewModel @Inject constructor(
	private val patternRepository: TimePatternRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
	private val userPreferencesRepository: UserPreferencesRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val navArgs: AddEditPatternScreenNavArgs = savedStateHandle.navArgs()
	private val patternId = navArgs.patternId
	
	private var defaultLessonDuration: Long? = null
	
	private val _uiState = MutableStateFlow(AddEditPatternUiState())
	val uiState: StateFlow<AddEditPatternUiState> = _uiState.asStateFlow()
	
	init {
		if (patternId != null) loadPattern(patternId)
		viewModelScope.launch(ioDispatcher) {
			defaultLessonDuration = userPreferencesRepository.getLessonDuration()
		}
	}
	
	/**
	 * Tries to save currently edited stroke.
	 *
	 * @return true if stroke was successfully added to list, false if not
	 */
	fun saveStroke(): Boolean {
		val lessonDuration =
			uiState.value.startTime.until(uiState.value.endTime, ChronoUnit.MINUTES)
		if (lessonDuration > 60) {
			_uiState.update { it.copy(errorMessage = R.string.too_long_lesson) }
			return false
		}
		
		val updatedStroke = PatternStrokeEntity(
			startTime = uiState.value.startTime,
			endTime = uiState.value.endTime,
			index = (uiState.value.index - 1),
			strokeId = uiState.value.strokeToUpdate?.strokeId ?: 0
		)
		
		val newStrokes =
			uiState.value.strokes.copyExclusively(targetItem = uiState.value.strokeToUpdate)
		
		val isIndexDuplicate = newStrokes.any { it.index == updatedStroke.index }
		if (isIndexDuplicate) {
			_uiState.update { it.copy(errorMessage = R.string.stroke_index_duplicate) }
			return false
		}
		
		val isStartTimeDuplicate = newStrokes.any { it.startTime == updatedStroke.startTime }
		if (isStartTimeDuplicate) {
			_uiState.update { it.copy(errorMessage = R.string.start_time_duplicate) }
			return false
		}
		
		val isEndTimeDuplicate = newStrokes.any { it.endTime == updatedStroke.endTime }
		if (isEndTimeDuplicate) {
			_uiState.update { it.copy(errorMessage = R.string.end_time_duplicate) }
			return false
		}
		
		val isTimingImproper = newStrokes.any { strokeFromList ->
			val existentStrokeIsEarlier = strokeFromList.startTime.isBefore(updatedStroke.startTime)
			val existentStrokeIsHigher = strokeFromList.index < updatedStroke.index
			
			return@any if (existentStrokeIsEarlier && !existentStrokeIsHigher) {
				_uiState.update { it.copy(errorMessage = R.string.too_early_time_for_such_index) }
				true
			} else if (!existentStrokeIsEarlier && existentStrokeIsHigher) {
				_uiState.update { it.copy(errorMessage = R.string.too_late_time_for_such_index) }
				true
			} else {
				false
			}
		}
		if (isTimingImproper) return false
		newStrokes.add(updatedStroke)
		
		_uiState.update {
			it.copy(
				strokeToUpdate = null,
				errorMessage = null,
				strokes = newStrokes.sortedBy { stroke -> stroke.index },
				startTime = it.endTime.plusMinutes(15),
				endTime = it.endTime.plusMinutes(60),
				index = it.index + 1
			)
		}
		return true
	}
	
	fun deleteStroke(stroke: PatternStrokeEntity) {
		val newStrokes = uiState.value.strokes.copyExclusively(stroke)
		
		_uiState.update {
			it.copy(strokes = newStrokes.sortedBy { stroke -> stroke.index })
		}
		showSnackbarMessage(R.string.successfully_deleted_stroke)
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update {
			it.copy(userMessage = message)
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	fun clearData() {
		_uiState.update {
			it.copy(
				strokeToUpdate = null,
				errorMessage = null,
				startTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
				endTime = LocalTime.now().truncatedTo(ChronoUnit.MINUTES),
				index = 1
			)
		}
	}
	
	fun onEndTimeSet(endTime: LocalTime) {
		_uiState.update { it.copy(endTime = endTime) }
	}
	
	fun onStartTimeSet(startTime: LocalTime) {
		_uiState.update {
			it.copy(
				startTime = startTime,
				endTime = startTime.plusMinutes(defaultLessonDuration ?: 45L)
			)
		}
	}
	
	fun updateName(name: String) {
		_uiState.update { it.copy(name = name) }
	}
	
	fun updateIndex(newIndex: Int) {
		_uiState.update { it.copy(index = newIndex) }
	}
	
	fun onStrokeClick(stroke: PatternStrokeEntity) {
		_uiState.update {
			it.copy(
				startTime = stroke.startTime,
				endTime = stroke.endTime,
				index = (stroke.index + 1),
				strokeToUpdate = stroke
			)
		}
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
	
	private fun createNewPattern() = viewModelScope.launch(ioDispatcher) {
		patternRepository.createPatternWithStrokes(uiState.value.name, uiState.value.strokes)
	}
	
	private fun updatePattern() {
		if (patternId == null) throw RuntimeException("updatePatternWithStrokes() was called but pattern is new.")
		
		viewModelScope.launch(ioDispatcher) {
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
		_uiState.update { it.copy(isLoading = true) }
		
		viewModelScope.launch(ioDispatcher) {
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