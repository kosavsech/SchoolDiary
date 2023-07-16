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
import com.kxsv.schooldiary.data.local.features.schedule.Schedule
import com.kxsv.schooldiary.data.local.features.schedule.ScheduleWithSubject
import com.kxsv.schooldiary.data.local.features.study_day.StudyDay
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStroke
import com.kxsv.schooldiary.data.network.schedule.NetworkSchedule
import com.kxsv.schooldiary.domain.AppDefaultsRepository
import com.kxsv.schooldiary.domain.PatternStrokeRepository
import com.kxsv.schooldiary.domain.ScheduleRepository
import com.kxsv.schooldiary.domain.StudyDayRepository
import com.kxsv.schooldiary.domain.SubjectRepository
import com.kxsv.schooldiary.domain.TimePatternRepository
import com.kxsv.schooldiary.util.copyExclusively
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
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
	private val subjectRepository: SubjectRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val studyDayRepository: StudyDayRepository,
	private val patternRepository: TimePatternRepository,
	private val appDefaultsRepository: AppDefaultsRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val dateStamp: Long? = savedStateHandle[AppDestinationsArgs.DATESTAMP_ARG]
	
	private val _uiState = MutableStateFlow(DayScheduleUiState())
	val uiState: StateFlow<DayScheduleUiState> = _uiState.asStateFlow()
	
	init {
		if (dateStamp != 0L && dateStamp != null) {
			Log.i(TAG, "init() loading from dateStamp($dateStamp)")
			onDayChangeUpdate(fromTimestamp(dateStamp))
		} else {
			Log.i(TAG, "init() loading from today")
			onDayChangeUpdate()
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
	
	fun deleteClass(lesson: ScheduleWithSubject) {
		val newClasses = copyExclusively(lesson, uiState.value.classes)
		_uiState.update {
			it.copy(
				classDetailed = null,
				classes = newClasses
			)
		}
		try {
			viewModelScope.launch {
				if (lesson.schedule.scheduleId == 0L) {
					Log.i(TAG, "deleteClass: tried to delete class w/o scheduleId")
					if (uiState.value.studyDay == null) {
						createEmptyStudyDay()
					}
					localiseNetSchedule()
				}
				scheduleRepository.deleteSchedule(lesson.schedule.scheduleId)
				showEditResultMessage(DELETE_RESULT_OK)
			}
		} catch (e: Exception) {
			Log.e(TAG, "deleteClass: couldn't delete class from repository", e)
		}
	}
	
	fun selectRefRange(startDate: LocalDate, endDate: LocalDate) {
		_uiState.update { it.copy(refRange = startDate..endDate) }
	}
	
	fun selectDestRange(startDate: LocalDate, endDate: LocalDate) {
		_uiState.update { it.copy(destRange = startDate..endDate) }
	}
	
	fun selectClass(lesson: ScheduleWithSubject) {
		_uiState.update { it.copy(classDetailed = lesson) }
	}
	
	fun unselectClass() {
		_uiState.update { it.copy(classDetailed = null) }
	}
	
	suspend fun createEmptyStudyDay(): Long {
		val defaultPatternId = appDefaultsRepository.getPatternId()
		val defaultPattern = patternRepository.getPattern(defaultPatternId)
		var appliedPatternId: Long? = null
		if (defaultPattern != null && defaultPatternId != 0L) {
			appliedPatternId = defaultPatternId
		}
		
		val newStudyDay = StudyDay(
			date = uiState.value.selectedDate,
			appliedPatternId = appliedPatternId,
		)
		val newStudyDayId = studyDayRepository.create(newStudyDay)
		if (newStudyDayId == 0L) throw RuntimeException("StudyDay wasn't created for some reason")
		_uiState.update {
			it.copy(studyDay = newStudyDay.copy(studyDayId = newStudyDayId))
		}
		return newStudyDayId
	}
	
	fun copySchedule(toCopyPattern: Boolean) {
		if (uiState.value.selectedCalendarDay == null)
			throw RuntimeException("copySchedule() was called but selectedCalendarDay is null.")
		viewModelScope.launch {
			copyLocalScheduleFromDate(
				fromDate = uiState.value.selectedCalendarDay!!.date,
				toDate = uiState.value.selectedDate,
				toCopyPattern = toCopyPattern
			)
		}
	}
	
	fun copyScheduleToRange(toCopyPattern: Boolean) {
		if (uiState.value.refRange == null)
			throw RuntimeException("copyScheduleToRange() was called but refRange is null.")
		
		val copyFromDays = uiState.value.refRange!!.rangeToList()
		val copyToDays = uiState.value.destRange!!.rangeToList(copyFromDays.size.toLong())
		
		for ((index, dateCopyTo) in copyToDays.withIndex()) {
			viewModelScope.launch {
				Log.d(
					TAG,
					"copyScheduleToRange() called with: dateCopyFrom = ${copyFromDays[index]}, dateCopyTo = $dateCopyTo"
				)
				copyLocalScheduleFromDate(
					fromDate = copyFromDays[index],
					toDate = dateCopyTo,
					toCopyPattern = toCopyPattern
				)
			}
		}
		_uiState.update { it.copy(refRange = null, destRange = null) }
	}
	
	fun updateOnCalendarDayChange(calendarDay: CalendarDay?) {
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
			Log.d(TAG, "updateOnCalendarDayChange() loading classes from $calendarDay.")
			loadScheduleOnDate(calendarDay.date)
		} else {
			Log.d(
				TAG,
				"updateOnCalendarDayChange: couldn't load classes because calendar day is null."
			)
		}
	}
	
	fun onDayChangeUpdate(date: LocalDate = LocalDate.now()) {
		Log.d(TAG, "onDayChangeUpdate() loading from date($date, ${localDateToTimestamp(date)}).")
		_uiState.update {
			it.copy(
				isLoading = true,
				studyDay = null,
				selectedDate = date,
				currentTimings = emptyList(),
				classes = emptyList()
			)
		}
		loadScheduleOnDate(date)
	}
	
	suspend fun getIdForClassFromNet(): Long {
		if (uiState.value.studyDay == null) {
			createEmptyStudyDay()
		}
		localiseNetSchedule()
		
		val indexOfClassDetailed = uiState.value.classDetailed!!.schedule.index
		val studyDayMasterId = uiState.value.studyDay!!.studyDayId
		return scheduleRepository.getByIdAndIndex(
			studyDayMasterId = studyDayMasterId,
			index = indexOfClassDetailed
		)?.scheduleId
			?: throw NoSuchElementException("Didn't find schedule with id($studyDayMasterId) and index($indexOfClassDetailed)")
	}
	
	suspend fun getDateStampForSchedule(): Long {
		if (uiState.value.studyDay == null) createEmptyStudyDay()
		localiseNetSchedule()
		return localDateToTimestamp(uiState.value.selectedDate)
	}
	
	private suspend fun copyLocalScheduleFromDate(
		fromDate: LocalDate, toDate: LocalDate, toCopyPattern: Boolean,
	) {
		_uiState.update { it.copy(isLoading = true) }
		val refStudyDay = studyDayRepository.getByDate(fromDate)
			?: throw NoSuchElementException("Reference StudyDay (date $fromDate) to copy from not found.")
		val toStudyDay = studyDayRepository.getByDate(toDate)
		
		val cloneDayId = if (toStudyDay != null) {
			// INFO: if copying schedule to existent study day, maybe with local schedules
			if (toCopyPattern) {
				studyDayRepository.update(
					toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
				)
			}
			scheduleRepository.deleteAllByDayId(toStudyDay.studyDayId)
			toStudyDay.studyDayId
		} else {
			val cloneStudyDay: StudyDay = if (toCopyPattern) {
				StudyDay(date = toDate, appliedPatternId = refStudyDay.appliedPatternId)
			} else {
				StudyDay(date = toDate)
			}
			studyDayRepository.create(cloneStudyDay)
		}
		
		val copyOfSchedules: MutableList<Schedule> = mutableListOf()
		scheduleRepository.getAllByMasterId(refStudyDay.studyDayId).forEach {
			copyOfSchedules.add(it.copy(studyDayMasterId = cloneDayId, scheduleId = 0))
		}
		scheduleRepository.upsertAll(copyOfSchedules)
		
		updateLocalScheduleOnDate(toDate)
	}
	
	/*private suspend fun getStudyDayOnDate(date: LocalDate): Pair<StudyDay, Boolean> {
		val existingStudyDay = studyDayRepository.getByDate(date)
		val studyDay = (if (existingStudyDay != null) {
			existingStudyDay
		} else {
			createEmptyStudyDay()
			uiState.value.studyDay
		})
			?: throw IllegalStateException("StudyDay is still null somehow")
		return Pair(studyDay, (existingStudyDay != null))
	}*/
	
	private suspend fun localiseNetSchedule() {
		try {
			val studyDay = uiState.value.studyDay
				?: throw NullPointerException("Shouldn't be called with null study day")
			
			val classes = uiState.value.classes
			if (classes.isEmpty()) throw IllegalArgumentException("Shouldn't be called with empty classes")
			
			val localSchedule = scheduleRepository.getAllByMasterId(studyDay.studyDayId)
			if (localSchedule.isNotEmpty()) throw RuntimeException("Schedule already localized")
			Log.i(TAG, "localiseNetSchedule: in process for ${uiState.value.studyDay}")
			
			val schedules = classes.map { it.schedule }
			var schedulesWithMasterDay = schedules
			val foundNullMasterDay = schedules.find { it.studyDayMasterId == null } != null
			
			if (foundNullMasterDay) {
				schedulesWithMasterDay =
					schedules.map { it.copy(studyDayMasterId = studyDay.studyDayId) }
			}
			scheduleRepository.upsertAll(schedulesWithMasterDay)
			
		} catch (e: NullPointerException) {
			Log.e(
				TAG, "localiseNetSchedule: couldn't localise" +
						" because couldn't check if schedule is localised", e
			)
		} catch (e: IllegalArgumentException) {
			Log.e(TAG, "localiseNetSchedule: couldn't localise because nothing to localise", e)
		} catch (e: RuntimeException) {
			Log.e(TAG, "localiseNetSchedule: failed to localise net schedule", e)
		}
	}
	
	/*private suspend fun copyScheduleFromNet(date: LocalDate) {
		try {
			val (studyDay, studyDayExisted) = getStudyDayOnDate(date)
			scheduleRepository.deleteAllByDayId(studyDay.studyDayId)
			
			val localizedNetClasses = scheduleRepository.loadFromNetworkByDate(date).toLocal()
			
			if (!studyDayExisted) {
				_uiState.update { it.copy(studyDay = studyDay) }
				localizedNetClasses.map { it.copy(studyDayMasterId = studyDay.studyDayId) }
			}
			scheduleRepository.upsertAll(localizedNetClasses)
			
		} catch (e: Exception) {
			Log.e(TAG, "copyScheduleFromNet: caught", e)
		}
	}*/
	
	private fun initializeNetworkScheduleOnDate(date: LocalDate) = viewModelScope.launch {
		val classes = measurePerformanceInMS(logger = { time, result ->
			Log.d(
				TAG,
				"initializeNetworkScheduleOnDate() performance is $time ms\nreturned: classes $result"
			)
		}) {
			scheduleRepository.loadFromNetworkByDate(date).toLocalWithSubject()
		}
		_uiState.update {
			it.copy(
				classes = classes,
				isLoading = false
			)
		}
	}
	
	private fun loadScheduleOnDate(date: LocalDate) {
		viewModelScope.coroutineContext.job.cancelChildren()
		viewModelScope.launch {
			studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date).let { dayWithClasses ->
				if (dayWithClasses != null) {
					_uiState.update { it.copy(studyDay = dayWithClasses.studyDay) }
					if (dayWithClasses.classes.isNotEmpty()) {
						Log.i(TAG, "loadScheduleOnDate: found local classes")
						_uiState.update { dayScheduleUiState ->
							dayScheduleUiState.copy(
								classes = dayWithClasses.classes.sortedBy { it.schedule.index },
								isLoading = false
							)
						}
					} else {
						// INFO: loads classes from network to UI with studyDayMasterId
						Log.i(TAG, "loadScheduleOnDate: searching for schedule in Net")
						initializeNetworkScheduleOnDate(date)
					}
				} else {
					// INFO: loads classes from network to UI without studyDayMasterId
					Log.i(
						TAG,
						"loadScheduleOnDate: study day is null, searching for schedule in Net"
					)
					initializeNetworkScheduleOnDate(date)
				}
				loadTimingsOnStudyDay(dayWithClasses?.studyDay)
			}
		}
	}
	
	/**
	 * Tries to load time pattern from passed [studyDay] if it's null or doesn't
	 * have time pattern specified, loads default pattern from datastore
	 * @see AppDefaultsRepository
	 * @param studyDay
	 */
	private suspend fun loadTimingsOnStudyDay(studyDay: StudyDay?) {
		val isFromDefaults = studyDay?.appliedPatternId == null
		val appliedPatternId = studyDay?.appliedPatternId ?: appDefaultsRepository.getPatternId()
		val strokes = measurePerformanceInMS(logger = { time, result ->
			Log.i(
				TAG, "loadTimingsOnStudyDay() performance is $time ms" +
						"\nloaded timings from ${if (isFromDefaults) "default " else ""}pattern(id: $appliedPatternId) ${result[0]}..."
			)
		}) {
			strokeRepository.getStrokesByPatternId(appliedPatternId)
		}
		
		_uiState.update {
			it.copy(currentTimings = strokes)
		}
	}
	
	private fun updateLocalScheduleOnDate(date: LocalDate) = viewModelScope.launch {
		studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date).let { dayWithClasses ->
			if (dayWithClasses != null) {
				_uiState.update { it.copy(studyDay = dayWithClasses.studyDay) }
				if (dayWithClasses.classes.isNotEmpty()) {
					_uiState.update { dayScheduleUiState ->
						dayScheduleUiState.copy(
							classes = dayWithClasses.classes.sortedBy { it.schedule.index },
							isLoading = false
						)
					}
				}
			} else {
				throw NoSuchElementException("Didn't found day with classes for some reason")
			}
			loadTimingsOnStudyDay(dayWithClasses.studyDay)
		}
	}
	
	private suspend fun NetworkSchedule.toLocalWithSubject(): ScheduleWithSubject {
		try {
			val subject =
				subjectRepository.getSubjectByName(subjectAncestorName)
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			
			val studyDay = studyDayRepository.getByDate(date)
			if (studyDay != null) {
				return ScheduleWithSubject(
					schedule = Schedule(
						index = index,
						subjectAncestorId = subject.subjectId,
						studyDayMasterId = studyDay.studyDayId
					),
					subject = subject,
				)
			} else {
				return ScheduleWithSubject(
					schedule = Schedule(
						index = index,
						subjectAncestorId = subject.subjectId,
					),
					subject = subject,
				)
			}
		} catch (e: NoSuchElementException) {
			throw RuntimeException("Failed to convert lesson toLocalWithSubject", e)
		}
	}
	
	private suspend fun List<NetworkSchedule>.toLocalWithSubject(): List<ScheduleWithSubject> {
		val newClasses = mutableListOf<ScheduleWithSubject>()
		return try {
			this.forEach { newClasses.add(it.toLocalWithSubject()) }
			newClasses
		} catch (e: RuntimeException) {
			Log.e(TAG, "List<NetworkSchedule>.toLocalWithSubject: classes are empty because", e)
			emptyList()
		}
	}
	
	/*private suspend fun NetworkSchedule.toLocal(): Schedule {
		try {
			val subject =
				subjectRepository.getSubjectByName(subjectAncestorName)
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			
			val studyDay = studyDayRepository.getByDate(date)
			return if (studyDay != null) {
				Schedule(
					index = index,
					subjectAncestorId = subject.subjectId,
					studyDayMasterId = studyDay.studyDayId
				)
			} else {
				Schedule(
					index = index,
					subjectAncestorId = subject.subjectId,
				)
			}
		} catch (e: NoSuchElementException) {
			throw RuntimeException("Failed to convert class toLocal", e)
		}
	}
	
	private suspend fun List<NetworkSchedule>.toLocal(): List<Schedule> {
		val newClasses = mutableListOf<Schedule>()
		return try {
			this.forEach { newClasses.add(it.toLocal()) }
			newClasses
		} catch (e: RuntimeException) {
			Log.e(TAG, "List<NetworkSchedule>.toLocal: classes are empty because", e)
			emptyList()
		}
	}*/
	
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
	
	private fun fromTimestamp(value: Long): LocalDate = if (value == 0L) LocalDate.now() else {
		Instant.ofEpochSecond(value).atZone(ZoneId.systemDefault()).toLocalDate()
	}
	
	private fun localDateToTimestamp(date: LocalDate): Long =
		date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()
	
	//the inline performance measurement method
	private inline fun <T> measurePerformanceInMS(
		logger: (Long, T) -> Unit,
		function: () -> T,
	): T {
		val startTime = System.currentTimeMillis()
		val result: T = function.invoke()
		val endTime = System.currentTimeMillis()
		logger.invoke(endTime - startTime, result)
		return result
	}
}