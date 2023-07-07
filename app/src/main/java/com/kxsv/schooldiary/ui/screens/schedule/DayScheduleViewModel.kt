package com.kxsv.schooldiary.ui.screens.schedule

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.AppDestinationsArgs
import com.kxsv.schooldiary.DELETE_RESULT_OK
import com.kxsv.schooldiary.EDIT_RESULT_OK
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.SELECTED_CUSTOM_PATTERN_OK
import com.kxsv.schooldiary.data.features.schedule.ScheduleWithSubject
import com.kxsv.schooldiary.data.features.study_day.StudyDay
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.domain.AppDefaultsRepository
import com.kxsv.schooldiary.domain.PatternStrokeRepository
import com.kxsv.schooldiary.domain.ScheduleRepository
import com.kxsv.schooldiary.domain.StudyDayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

private const val TAG = "DayScheduleViewModel"

data class DayScheduleUiState(
	val studyDay: StudyDay? = null,
	val classes: List<ScheduleWithSubject> = emptyList(),
	val currentPattern: List<PatternStroke> = emptyList(),
	val classDetailed: ScheduleWithSubject? = null,
	val date: LocalDate = LocalDate.now(),
	val userMessage: Int? = null,
	val isLoading: Boolean = false,
	val isClassDeleted: Boolean = false,
)

@HiltViewModel
class DayScheduleViewModel @Inject constructor(
	private val scheduleRepository: ScheduleRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val studyDayRepository: StudyDayRepository,
	private val appDefaultsRepository: AppDefaultsRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val dateStamp: Long = savedStateHandle[AppDestinationsArgs.DATESTAMP_ARG]!!
	
	private val _uiState = MutableStateFlow(DayScheduleUiState())
	val uiState: StateFlow<DayScheduleUiState> = _uiState.asStateFlow()
	
	init {
		if (dateStamp != 0L) {
			Log.d(TAG, "init() loading from dateStamp($dateStamp)")
			updateDayWithClasses(fromTimestamp(dateStamp))
		} else {
			Log.d(TAG, "init() loading from today")
			updateDayWithClasses()
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_schedule_message)
			ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_schedule_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_schedule_message)
			SELECTED_CUSTOM_PATTERN_OK -> showSnackbarMessage(R.string.successfully_set_custom_pattern)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	fun deleteClass(scheduleId: Long) = viewModelScope.launch {
		scheduleRepository.deleteSchedule(scheduleId)
		_uiState.update {
			it.copy(
				classDetailed = null,
				isClassDeleted = true
			)
		}
	}
	
	fun selectClass(lesson: ScheduleWithSubject) {
		_uiState.update { it.copy(classDetailed = lesson) }
	}
	
	fun unselectClass() {
		_uiState.update { it.copy(classDetailed = null) }
	}
	
	fun onDeleteClass() {
		showEditResultMessage(DELETE_RESULT_OK)
	}
	
	fun updateDayWithClasses(date: LocalDate = LocalDate.now()) {
		Log.d(
			TAG, "updateDayWithClasses() loading from date $date"
		)
		_uiState.update { it.copy(isLoading = true) }
		_uiState.update {
			it.copy(
				studyDay = null,
				currentPattern = emptyList(),
				classes = emptyList(),
				date = date
			)
		}
		
		viewModelScope.launch {
			studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date)
				.let { dayWithClasses ->
					if (dayWithClasses != null) {
						_uiState.update {
							it.copy(
								studyDay = dayWithClasses.studyDay,
								classes = dayWithClasses.classes,
							)
						}
					}
					val appliedPatternId = dayWithClasses?.studyDay?.appliedPatternId
						?: appDefaultsRepository.getPatternId()
					
					// TODO: check if it can return null
					strokeRepository.getStrokesByPatternId(appliedPatternId)
						.let { strokes ->
							_uiState.update {
								it.copy(
									currentPattern = strokes,
									isLoading = false
								)
							}
						}
				}
		}
	}
}

private fun fromTimestamp(value: Long): LocalDate = if (value == 0L) {
	LocalDate.now()
} else {
	Instant.ofEpochSecond(value).atZone(ZoneId.of("UTC")).toLocalDate()
}