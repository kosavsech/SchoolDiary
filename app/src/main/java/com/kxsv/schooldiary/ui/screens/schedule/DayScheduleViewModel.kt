package com.kxsv.schooldiary.ui.screens.schedule

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.CalendarDay
import com.kxsv.schooldiary.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.AppDestinationsArgs
import com.kxsv.schooldiary.DELETE_RESULT_OK
import com.kxsv.schooldiary.EDIT_RESULT_OK
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.SELECTED_CUSTOM_PATTERN_OK
import com.kxsv.schooldiary.data.features.schedule.Schedule
import com.kxsv.schooldiary.data.features.schedule.ScheduleWithSubject
import com.kxsv.schooldiary.data.features.study_day.StudyDay
import com.kxsv.schooldiary.data.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.domain.AppDefaultsRepository
import com.kxsv.schooldiary.domain.PatternStrokeRepository
import com.kxsv.schooldiary.domain.ScheduleRepository
import com.kxsv.schooldiary.domain.StudyDayRepository
import com.kxsv.schooldiary.util.copyExclusively
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject


private const val TAG = "DayScheduleViewModel"

data class DayScheduleUiState(
	val studyDay: StudyDay? = null,
	val classes: List<ScheduleWithSubject> = emptyList(),
	val currentTimings: List<PatternStroke> = emptyList(),
	val classDetailed: ScheduleWithSubject? = null,
	val selectedDate: LocalDate = LocalDate.now(), // unique for DayScheduleScreen
	val selectedCalendarDay: CalendarDay? = null, // unique for DayScheduleCopyScreen
	val refRange: ClosedRange<LocalDate>? = null,
	val destRange: ClosedRange<LocalDate>? = null,
	val userMessage: Int? = null,
	val isLoading: Boolean = false,
)

@HiltViewModel
class DayScheduleViewModel @Inject constructor(
	private val scheduleRepository: ScheduleRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val studyDayRepository: StudyDayRepository,
	private val appDefaultsRepository: AppDefaultsRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val dateStamp: Long? = savedStateHandle[AppDestinationsArgs.DATESTAMP_ARG]
	
	private val _uiState = MutableStateFlow(DayScheduleUiState())
	val uiState: StateFlow<DayScheduleUiState> = _uiState.asStateFlow()
	
	init {
		if (dateStamp != 0L && dateStamp != null) {
			Log.d(TAG, "init() loading from dateStamp($dateStamp)")
			updateDay(fromTimestamp(dateStamp))
		} else {
			Log.d(TAG, "init() loading from today")
			updateDay()
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
	
	fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	fun deleteClass(lesson: ScheduleWithSubject) = viewModelScope.launch {
		if (lesson.schedule.scheduleId != 0L) scheduleRepository.deleteSchedule(lesson.schedule.scheduleId)
		
		val newClasses = copyExclusively(lesson, uiState.value.classes)
		_uiState.update {
			it.copy(
				classDetailed = null,
				classes = newClasses
			)
		}
		showEditResultMessage(DELETE_RESULT_OK)
	}
	
	fun selectRefRange(startDate: LocalDate, endDate: LocalDate) {
		_uiState.update { it.copy(refRange = startDate..endDate) }
	}
	
	private fun selectDestRange(startDate: LocalDate, endDate: LocalDate) {
		_uiState.update { it.copy(destRange = startDate..endDate) }
	}
	
	fun selectClass(lesson: ScheduleWithSubject) {
		_uiState.update { it.copy(classDetailed = lesson) }
	}
	
	fun unselectClass() {
		_uiState.update { it.copy(classDetailed = null) }
	}
	
	fun updateCalendarDay(calendarDay: CalendarDay?) {
		_uiState.update {
			it.copy(
				isLoading = true,
				studyDay = null,
				selectedCalendarDay = calendarDay,
				currentTimings = emptyList(),
				classes = emptyList()
			)
		}
		if (calendarDay != null) {
			Log.d(TAG, "updateCalendarDay() downloading classes from $calendarDay.")
			downloadClassesOnDate(calendarDay.date)
		} else {
			Log.d(TAG, "updateCalendarDay: couldn't download classes because calendar day is null.")
		}
	}
	
	fun updateDay(date: LocalDate = LocalDate.now()) {
		Log.d(TAG, "updateDay() loading from date $date.")
		_uiState.update {
			it.copy(
				isLoading = true,
				studyDay = null,
				selectedDate = date,
				currentTimings = emptyList(),
				classes = emptyList()
			)
		}
		downloadClassesOnDate(date)
	}
	
	fun copySchedule(toCopyPattern: Boolean) {
		if (uiState.value.selectedCalendarDay == null)
			throw RuntimeException("copySchedule() was called but selectedCalendarDay is null.")
		viewModelScope.launch {
			copyScheduleFromDate(
				fromDate = uiState.value.selectedCalendarDay!!.date,
				toDate = uiState.value.selectedDate,
				toCopyPattern = toCopyPattern
			)
		}
	}
	
	private fun ClosedRange<LocalDate>.rangeToList(limiter: Long? = null): List<LocalDate> {
		val dates = mutableListOf(this.start)
		var daysAdded = 1L
		while (daysAdded <= ChronoUnit.DAYS.between(this.start, this.endInclusive)) {
			dates.add(this.start.plusDays(daysAdded))
			daysAdded++
			if (limiter != null && limiter == daysAdded) break
		}
		Log.d(TAG, "rangeToList() called on range = $this and returns $dates")
		return dates
	}
	
	fun copyScheduleToRange(startDate: LocalDate, endDate: LocalDate) {
		if (uiState.value.refRange == null)
			throw RuntimeException("copyScheduleToRange() was called but refRange is null.")
		selectDestRange(startDate, endDate)
		
		val copyFromDays = uiState.value.refRange!!.rangeToList()
		val copyToDays = uiState.value.destRange!!.rangeToList(copyFromDays.size.toLong())
		
		for ((index, dateCopyTo) in copyToDays.withIndex()) {
			viewModelScope.launch {
				Log.d(
					TAG,
					"copyScheduleToRange() called with: dateCopyFrom = ${copyFromDays[index]}, dateCopyTo = $dateCopyTo"
				)
				copyScheduleFromDate(
					fromDate = copyFromDays[index],
					toDate = dateCopyTo,
					toCopyPattern = true
				)
			}
		}
		_uiState.update { it.copy(refRange = null, destRange = null) }
	}
	
	private suspend fun copyScheduleFromDate(
		fromDate: LocalDate, toDate: LocalDate, toCopyPattern: Boolean,
	) {
		val refStudyDay = studyDayRepository.getStudyDayByDate(fromDate)
		if (refStudyDay == null) {
			Log.d(
				TAG,
				"copyScheduleFromDate() called with: fromDate = $fromDate, toDate = $toDate, toCopyPattern = $toCopyPattern" +
						"\nReference StudyDay (date $fromDate) to copy from not found."
			)
			return
			/*throw NoSuchElementException("Reference StudyDay (date $fromDate) to copy from not found.")*/
		}
		val toStudyDay = studyDayRepository.getStudyDayByDate(toDate)
		
		val idOfDestinationDay = if (toStudyDay != null) {
			if (toCopyPattern) {
				studyDayRepository.update(
					toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
				)
			}
			toStudyDay.studyDayId
		} else {
			var copyDay = refStudyDay.copy(date = toDate, studyDayId = 0)
			if (!toCopyPattern) {
				copyDay = copyDay.copy(appliedPatternId = null)
			}
			studyDayRepository.create(copyDay)
		}
		
		toStudyDay?.studyDayId?.let { scheduleRepository.deleteAllByDayId(it) }
		val copyOfSchedules: MutableList<Schedule> = mutableListOf()
		
		scheduleRepository.getAllByMasterId(refStudyDay.studyDayId).forEach {
			copyOfSchedules.add(it.copy(studyDayMasterId = idOfDestinationDay, scheduleId = 0))
		}
		scheduleRepository.upsertAll(copyOfSchedules)
		
		downloadClassesOnDate(toDate)
	}
	
	private fun downloadClassesOnDate(date: LocalDate) = viewModelScope.launch {
		studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date).let { dayWithClasses ->
//			Log.d(TAG, "downloadClassesOnDate(): dayWithClasses = $dayWithClasses")
			if (dayWithClasses != null) {
				_uiState.update {
					it.copy(studyDay = dayWithClasses.studyDay, classes = dayWithClasses.classes)
				}
			}
			val appliedPatternId = dayWithClasses?.studyDay?.appliedPatternId
				?: appDefaultsRepository.getPatternId()
			
			strokeRepository.getStrokesByPatternId(appliedPatternId).let { strokes ->
//				Log.d(TAG, "downloadClassesOnDate(): strokes = $strokes")
				_uiState.update {
					it.copy(currentTimings = strokes, isLoading = false)
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