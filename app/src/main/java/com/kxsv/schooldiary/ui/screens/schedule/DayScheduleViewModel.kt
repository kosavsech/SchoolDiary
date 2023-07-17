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
import com.kxsv.schooldiary.domain.AppSettingsRepository
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
	val selectedRefCalendarDay: CalendarDay? = null, // unique for DayScheduleCopyScreen
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
	private val appSettingsRepository: AppSettingsRepository,
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
				classDetailed = null, classes = newClasses
			)
		}
		try {
			viewModelScope.launch {
				if (lesson.schedule.scheduleId == 0L) {
					Log.i(TAG, "deleteClass: tried to delete class w/o scheduleId.")
					localiseCurrentNetSchedule()
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
	
	private suspend fun createNewStudyDay(
		date: LocalDate = uiState.value.selectedDate,
		predefinedPatternId: Long? = null,
	) {
		var appliedPatternId = predefinedPatternId
		if (predefinedPatternId == null) {
			// INFO: try to get default one, or will be filled with null
			val defaultPatternId = appSettingsRepository.getPatternId()
			val defaultPattern = patternRepository.getPattern(defaultPatternId)
			
			if (defaultPattern != null && defaultPatternId != 0L) {
				appliedPatternId = defaultPatternId
			}
		}
		
		val newStudyDay = StudyDay(
			date = date,
			appliedPatternId = appliedPatternId,
		)
		val newStudyDayId = studyDayRepository.create(newStudyDay)
		if (newStudyDayId == 0L) throw RuntimeException("StudyDay wasn't created for some reason")
		_uiState.update {
			it.copy(studyDay = newStudyDay.copy(studyDayId = newStudyDayId))
		}
	}
	
	fun copySchedule(shouldCopyPattern: Boolean) {
		if (uiState.value.selectedRefCalendarDay == null)
			throw RuntimeException("copySchedule() was called but selectedCalendarDay is null.")
		_uiState.update { it.copy(isLoading = true) }
		val fromDate = uiState.value.selectedRefCalendarDay!!.date
		val toDate = uiState.value.selectedDate
		viewModelScope.launch {
			if (isScheduleRemoteOnDate(fromDate)) {
				Log.d(TAG, "copySchedule() isScheduleRemoteOnDate = true (fromDate = $fromDate)")
				copyRemoteScheduleToCurrentDay(
					fromDate = fromDate,
					shouldCopyPattern = shouldCopyPattern
				)
			} else {
				Log.d(TAG, "copySchedule() isScheduleRemoteOnDate = false (fromDate = $fromDate)")
				copyLocalScheduleFromDate(
					fromDate = fromDate,
					toDate = toDate,
					shouldCopyPattern = shouldCopyPattern
				)
			}
		}
	}
	
	fun copyScheduleToRange(toCopyPattern: Boolean) {
		if (uiState.value.refRange == null)
			throw RuntimeException("copyScheduleToRange() was called but refRange is null.")
		_uiState.update { it.copy(isLoading = true) }
		
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
					shouldCopyPattern = toCopyPattern
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
				selectedRefCalendarDay = calendarDay,
				currentTimings = emptyList(),
				classes = emptyList()
			)
		}
		if (calendarDay != null) {
			Log.i(TAG, "updateOnCalendarDayChange() loading classes from $calendarDay.")
			loadScheduleOnDate(calendarDay.date)
		} else {
			Log.d(
				TAG,
				"updateOnCalendarDayChange: couldn't load classes because calendar day is null."
			)
		}
	}
	
	fun onDayChangeUpdate(date: LocalDate = LocalDate.now()) {
		Log.i(TAG, "onDayChangeUpdate() loading from date($date, ${localDateToTimestamp(date)}).")
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
		localiseCurrentNetSchedule()
		
		val indexOfClassDetailed = uiState.value.classDetailed!!.schedule.index
		val studyDayMasterId = uiState.value.studyDay!!.studyDayId
		return scheduleRepository.getByIdAndIndex(
			studyDayMasterId = studyDayMasterId,
			index = indexOfClassDetailed
		)?.scheduleId
			?: throw NoSuchElementException("Didn't find schedule with id($studyDayMasterId) and index($indexOfClassDetailed)")
	}
	
	/**
	 * Copies remote schedule from [specific day][fromDate] to [another day][toDate].
	 * Does DB call, so should be time effective.
	 *
	 * @param fromDate
	 * @param toDate
	 * @param shouldCopyPattern determines whether the method will copy applied pattern id from
	 * reference study day
	 * @see copyRemoteScheduleToCurrentDay
	 * @see copyRemoteSchedule
	 */
	private suspend fun copyLocalScheduleFromDate(
		fromDate: LocalDate, toDate: LocalDate, shouldCopyPattern: Boolean,
	) {
		try {
			val refStudyDay = studyDayRepository.getByDate(fromDate)
				?: throw NoSuchElementException("Reference StudyDay (date $fromDate) to copy from not found.")
			val toStudyDay = studyDayRepository.getByDate(toDate)
			
			val cloneDayId = if (toStudyDay != null) {
				// INFO: if copying schedule to existent study day,
				//  maybe with local schedules which should be deleted first.
				if (shouldCopyPattern) {
					studyDayRepository.update(
						toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
					)
				}
				scheduleRepository.deleteAllByDayId(toStudyDay.studyDayId)
				toStudyDay.studyDayId
			} else {
				if (shouldCopyPattern) {
					createNewStudyDay(
						date = toDate,
						predefinedPatternId = refStudyDay.appliedPatternId
					)
				} else {
					createNewStudyDay(date = toDate)
				}
				uiState.value.studyDay?.studyDayId
			}
			Log.d(
				TAG,
				"copyLocalScheduleFromDate() called with toStudyDay $toStudyDay\n" +
						"fromStudyDay $refStudyDay"
			)
			val copyOfSchedules: MutableList<Schedule> = mutableListOf()
			scheduleRepository.getAllByMasterId(refStudyDay.studyDayId).forEach {
				copyOfSchedules.add(it.copy(studyDayMasterId = cloneDayId, scheduleId = 0))
			}
			scheduleRepository.upsertAll(copyOfSchedules)
			
			updateLocalScheduleOnDate(toDate)
		} catch (e: NoSuchElementException) {
			Log.e(
				TAG, "copyLocalScheduleFromDate: couldn't copy schedule because nowhere from to", e
			)
		}
	}
	
	/**
	 * Copies remote schedule from [specific day][fromDate] to [another day][toDate].
	 * Does 1 network call, so costly in time.
	 *
	 * @param fromDate
	 * @param toDate
	 * @param shouldCopyPattern determines whether the method will copy applied pattern id from
	 * reference study day
	 * @see copyRemoteScheduleToCurrentDay
	 */
	private suspend fun copyRemoteSchedule(
		fromDate: LocalDate, toDate: LocalDate, shouldCopyPattern: Boolean,
	) {
		try {
			val refStudyDay = studyDayRepository.getByDate(fromDate)
			val toStudyDay = studyDayRepository.getByDate(toDate)
			
			val cloneDayId = if (toStudyDay != null) {
				// INFO: if copying schedule to existent study day,
				//  maybe with local schedules which should be deleted first.
				if (shouldCopyPattern && refStudyDay != null) {
					studyDayRepository.update(
						toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
					)
				}
				scheduleRepository.deleteAllByDayId(toStudyDay.studyDayId)
				toStudyDay.studyDayId
			} else {
				if (shouldCopyPattern && refStudyDay != null) {
					createNewStudyDay(
						date = toDate,
						predefinedPatternId = refStudyDay.appliedPatternId
					)
				} else {
					createNewStudyDay(date = toDate)
				}
				uiState.value.studyDay?.studyDayId
			} ?: throw RuntimeException("Couldn't retrieve studyDayId")
			Log.w(
				TAG,
				"copyRemoteSchedule: uiState.value.classes = ${uiState.value.classes}"
			)
			val localizedNetClasses =
				scheduleRepository.loadFromNetworkByDate(fromDate).toLocal(cloneDayId)
			
			scheduleRepository.upsertAll(localizedNetClasses)
			
			updateLocalScheduleOnDate(toDate)
		} catch (e: Exception) {
			Log.e(TAG, "copyRemoteSchedule: caught", e)
		}
	}
	
	/**
	 * Copies remote schedule from [specific day][fromDate] to [current day][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.selectedDate].
	 * Does not make network calls, so time efficient.
	 *
	 * @param fromDate
	 * @param shouldCopyPattern determines whether the method will copy applied pattern id from
	 * reference study day
	 * @see copyRemoteSchedule
	 */
	private suspend fun copyRemoteScheduleToCurrentDay(
		fromDate: LocalDate, shouldCopyPattern: Boolean,
	) {
		try {
			val refStudyDay = studyDayRepository.getByDate(fromDate)
			val toStudyDay = studyDayRepository.getByDate(uiState.value.selectedDate)
			
			val cloneDayId = if (toStudyDay != null) {
				// INFO: if copying schedule to existent study day,
				//  maybe with local schedules which should be deleted first.
				if (shouldCopyPattern && refStudyDay != null) {
					studyDayRepository.update(
						toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
					)
				}
				scheduleRepository.deleteAllByDayId(toStudyDay.studyDayId)
				toStudyDay.studyDayId
			} else {
				if (shouldCopyPattern && refStudyDay != null) {
					createNewStudyDay(
						date = uiState.value.selectedDate,
						predefinedPatternId = refStudyDay.appliedPatternId
					)
				} else {
					createNewStudyDay(date = uiState.value.selectedDate)
				}
				uiState.value.studyDay?.studyDayId
			} ?: throw RuntimeException("Couldn't retrieve studyDayId")
			
			val localizedNetClasses =
				uiState.value.classes.map { it.schedule.copy(studyDayMasterId = cloneDayId) }
			
			scheduleRepository.upsertAll(localizedNetClasses)
			
			updateLocalScheduleOnDate(uiState.value.selectedDate)
		} catch (e: Exception) {
			Log.e(TAG, "copyRemoteSchedule: caught", e)
		}
	}
	
	/**
	 * Does whatever it takes to get study day by [date]. If doesn't exist, [creates new][createNewStudyDay].
	 *
	 * @param date
	 * @return [StudyDay]
	 */
	suspend fun getStudyDayForced(date: LocalDate): StudyDay {
		val existingStudyDay = studyDayRepository.getByDate(date)
		val studyDay = (if (existingStudyDay != null) {
			existingStudyDay
		} else {
			createNewStudyDay()
			uiState.value.studyDay
		})
			?: throw IllegalStateException("StudyDay is still null somehow")
		return studyDay
	}
	
	/**
	 * Does whatever it takes to get study day for current [date][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.selectedDate].
	 * If doesn't exist [creates new][createNewStudyDay].
	 *
	 * @return [StudyDay]
	 */
	suspend fun getCurrentStudyDayForced(): StudyDay {
		uiState.value.studyDay ?: createNewStudyDay()
		return uiState.value.studyDay
			?: throw IllegalStateException("StudyDay is still null somehow")
	}
	
	private fun initializeNetworkScheduleOnDate(date: LocalDate) = viewModelScope.launch {
		val classes = measurePerformanceInMS(logger = { time, result ->
			Log.i(
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
	
	fun isScheduleRemote(
		classes: List<ScheduleWithSubject>? = null,
		schedules: List<Schedule>? = null,
	): Boolean {
		val schedulesIds =
			classes?.map { it.schedule.scheduleId }
				?: (schedules?.map { it.scheduleId }
					?: uiState.value.classes.map { it.schedule.scheduleId })
		return schedulesIds.binarySearch(0L) != -1
	}
	
	private fun currentClassesHasMasterId(): Boolean {
		val schedulesMasterIds = uiState.value.classes.map { it.schedule.studyDayMasterId }
		return schedulesMasterIds.binarySearch(null) == -1
	}
	
	/**
	 * Used for adding network schedule to local DB. If classes don't have [studyDayMasterId][com.kxsv.schooldiary.data.local.features.schedule.Schedule.studyDayMasterId]
	 * adds it for each class
	 *
	 * @throws IllegalStateException when study day is null
	 * @throws IllegalArgumentException when classes are empty
	 * @throws RuntimeException if found local schedule for that day
	 */
	suspend fun localiseCurrentNetSchedule() {
		try {
			val studyDay = getCurrentStudyDayForced()
			
			val classes = uiState.value.classes
			if (classes.isEmpty()) throw IllegalArgumentException("Shouldn't be called with empty classes")
			
			val localSchedule = scheduleRepository.getAllByMasterId(studyDay.studyDayId)
			if (localSchedule.isNotEmpty()) throw RuntimeException("Schedule already localized")
			
			Log.i(TAG, "localiseCurrentNetSchedule: in process for ${uiState.value.studyDay}")
			val schedules = classes.map { it.schedule }
			
			if (currentClassesHasMasterId()) {
				scheduleRepository.upsertAll(schedules)
			} else {
				val schedulesWithMasterDay =
					schedules.map { it.copy(studyDayMasterId = studyDay.studyDayId) }
				scheduleRepository.upsertAll(schedulesWithMasterDay)
			}
			
		} catch (e: IllegalStateException) {
			Log.e(
				TAG, "localiseCurrentNetSchedule: couldn't localise" +
						" because cannot check if schedule is localised", e
			)
		} catch (e: IllegalArgumentException) {
			Log.e(
				TAG,
				"localiseCurrentNetSchedule: couldn't localise because nothing to localise", e
			)
		} catch (e: RuntimeException) {
			Log.e(TAG, "localiseCurrentNetSchedule: failed to localise net schedule", e)
		}
	}
	
	private fun loadScheduleOnDate(date: LocalDate) {
		viewModelScope.coroutineContext.job.cancelChildren()
		viewModelScope.launch {
			val dayWithClasses = measurePerformanceInMS(logger = { time, _ ->
				Log.i(TAG, "getDayAndSchedulesWithSubjectsByDate() performance is $time ms")
			}) {
				studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date)
			}
			if (dayWithClasses != null) {
				_uiState.update { it.copy(studyDay = dayWithClasses.studyDay) }
				if (dayWithClasses.classes.isNotEmpty()) {
					Log.i(TAG, "loadScheduleOnDate: found local schedule")
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
					"loadScheduleOnDate: studyDay is NULL, searching for schedule in Net"
				)
				initializeNetworkScheduleOnDate(date)
			}
			updateTimingsOnStudyDay(dayWithClasses?.studyDay)
		}
	}
	
	private suspend fun isScheduleRemoteOnDate(date: LocalDate): Boolean {
		val dayWithClasses = studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date)
		return dayWithClasses?.classes?.isEmpty() ?: true
	}
	
	/**
	 * Tries to load time pattern from passed [studyDay] if it's null or doesn't
	 * have time pattern specified, loads default pattern from [datastore][com.kxsv.schooldiary.data.app_settings.AppSettings]
	 *
	 * @param studyDay
	 */
	private suspend fun updateTimingsOnStudyDay(studyDay: StudyDay?) {
		val isFromDefaults = studyDay?.appliedPatternId == null
		val appliedPatternId = studyDay?.appliedPatternId ?: appSettingsRepository.getPatternId()
		val strokes = measurePerformanceInMS(logger = { time, result ->
			Log.i(
				TAG, "updateTimingsOnStudyDay() performance is $time ms" +
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
				_uiState.update { dayScheduleUiState ->
					dayScheduleUiState.copy(
						studyDay = dayWithClasses.studyDay,
						classes = dayWithClasses.classes.sortedBy { it.schedule.index },
						isLoading = false
					)
				}
			} else {
				throw NoSuchElementException("Didn't found day with classes for some reason")
			}
			updateTimingsOnStudyDay(dayWithClasses.studyDay)
		}
	}
	
	private suspend fun NetworkSchedule.toLocalWithSubject(): ScheduleWithSubject {
		try {
			val subject =
				subjectRepository.getSubjectByName(subjectAncestorName)
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			// TODO: add prompt to create subject with such name, or create it forcibly
			
			val studyDay = studyDayRepository.getByDate(date)
			val localizedClass = ScheduleWithSubject(
				schedule = Schedule(
					index = index,
					subjectAncestorId = subject.subjectId,
				),
				subject = subject,
			)
			return if (studyDay != null) {
				localizedClass.copy(schedule = localizedClass.schedule.copy(studyDayMasterId = studyDay.studyDayId))
			} else {
				localizedClass
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
	
	private suspend fun NetworkSchedule.toLocal(studyDayMasterId: Long?): Schedule {
		try {
			val subject =
				subjectRepository.getSubjectByName(subjectAncestorName)
					?: throw NoSuchElementException("Not found subject with name $subjectAncestorName")
			// TODO: add prompt to create subject with such name, or create it forcibly
			
			val studyDay = studyDayRepository.getByDate(date)
			val localizedClass = Schedule(
				index = index,
				subjectAncestorId = subject.subjectId,
			)
			return if (studyDayMasterId != null) {
				localizedClass.copy(studyDayMasterId = studyDayMasterId)
			} else if (studyDay != null) {
				localizedClass.copy(studyDayMasterId = studyDay.studyDayId)
			} else {
				localizedClass
			}
		} catch (e: NoSuchElementException) {
			throw RuntimeException("Failed to convert class toLocal", e)
		}
	}
	
	/**
	 * Convert [NetworkSchedule] to local, possibly to push the [studyDayMasterId]
	 * which will be assigned to each [Schedule] after conversion, which is by default
	 * is [current studyDay][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.studyDay]
	 * id or is null.
	 *
	 * @param studyDayMasterId value to assign to each class [studyDayMasterId][com.kxsv.schooldiary.data.local.features.schedule.Schedule.studyDayMasterId] field
	 * @return
	 */
	private suspend fun List<NetworkSchedule>.toLocal(studyDayMasterId: Long? = null): List<Schedule> {
		val newClasses = mutableListOf<Schedule>()
		return try {
			this.forEach { newClasses.add(it.toLocal(studyDayMasterId)) }
			newClasses
		} catch (e: RuntimeException) {
			Log.e(TAG, "List<NetworkSchedule>.toLocal: classes are empty because", e)
			emptyList()
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