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
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.domain.AppSettingsRepository
import com.kxsv.schooldiary.domain.PatternStrokeRepository
import com.kxsv.schooldiary.domain.ScheduleNetworkDataSource
import com.kxsv.schooldiary.domain.ScheduleRepository
import com.kxsv.schooldiary.domain.StudyDayRepository
import com.kxsv.schooldiary.domain.SubjectRepository
import com.kxsv.schooldiary.domain.TimePatternRepository
import com.kxsv.schooldiary.util.NetworkException
import com.kxsv.schooldiary.util.copyExclusively
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject


private const val TAG = "DayScheduleViewModel"

data class DayScheduleUiState(
	val studyDay: StudyDay? = null,
	val classes: Map<Int, ScheduleWithSubject> = emptyMap(),
	val fetchedClasses: Map<Int, ScheduleWithSubject>? = null,
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
	private val netScheduleDataSource: ScheduleNetworkDataSource,
	private val subjectRepository: SubjectRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val studyDayRepository: StudyDayRepository,
	private val patternRepository: TimePatternRepository,
	private val appSettingsRepository: AppSettingsRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
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
		val newClasses = copyExclusively(
			targetItemKey = lesson.schedule.index,
			elements = uiState.value.classes
		)
		_uiState.update {
			it.copy(
				classDetailed = null, classes = newClasses
			)
		}
		try {
			viewModelScope.launch(ioDispatcher) {
				if (lesson.schedule.scheduleId == 0L) {
					Log.i(TAG, "deleteClass: tried to delete class w/o scheduleId.")
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
	
	/**
	 * Create new study day
	 *
	 * @param date
	 * @param predefinedPatternId
	 * @throws RuntimeException if couldn't create study day
	 */
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
		viewModelScope.launch(ioDispatcher) {
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
			viewModelScope.launch(ioDispatcher) {
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
				classes = emptyMap(),
				fetchedClasses = null,
			)
		}
		if (calendarDay != null) {
			Log.i(TAG, "updateOnCalendarDayChange() loading classes from $calendarDay.")
			retrieveScheduleOnDate(calendarDay.date)
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
				classes = emptyMap(),
				fetchedClasses = null
			)
		}
		retrieveScheduleOnDate(date)
	}
	
	suspend fun getIdForClassFromNet(): Long {
		localiseNetSchedule()
		
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
			
			loadLocalScheduleOnDate(toDate)
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
				netScheduleDataSource.loadScheduleForDate(fromDate).toLocal(cloneDayId)
			
			scheduleRepository.upsertAll(localizedNetClasses)
			
			loadLocalScheduleOnDate(toDate)
		} catch (e: NetworkException) {
			Log.e(TAG, "copyRemoteSchedule: exception on login", e)
		} catch (e: IOException) {
			Log.e(TAG, "copyRemoteSchedule: exception on response parse", e)
		} catch (e: Exception) {
			Log.e(TAG, "copyRemoteSchedule: exception", e)
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
				uiState.value.classes.map { it.value.schedule.copy(studyDayMasterId = cloneDayId) }
			
			scheduleRepository.upsertAll(localizedNetClasses)
			
			loadLocalScheduleOnDate(uiState.value.selectedDate)
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
	 * @throws IllegalStateException if didn't succeed to create newStudyDay
	 */
	suspend fun getCurrentStudyDayForced(): StudyDay {
//		appSettingsRepository.setAuthCookie(null)
		uiState.value.studyDay ?: createNewStudyDay()
		return uiState.value.studyDay
			?: throw IllegalStateException("StudyDay is still null somehow")
	}
	
	private fun initializeNetworkScheduleOnDate(date: LocalDate) =
		viewModelScope.launch(ioDispatcher) {
			try {
				val classes = measurePerformanceInMS(logger = { time, result ->
					Log.i(
						TAG,
						"initializeNetworkScheduleOnDate: loadScheduleForDate() performance is $time ms" +
								"\nreturned: classes $result"
					)
				}) {
					withTimeout(10000L) {
						netScheduleDataSource.loadScheduleForDate(date).toLocalWithSubject()
					}
				}
				_uiState.update {
					it.copy(
						classes = classes,
						isLoading = false
					)
				}
			} catch (e: NetworkException) {
				Log.e(TAG, "initializeNetworkScheduleOnDate: exception on login", e)
			} catch (e: IOException) {
				Log.e(TAG, "initializeNetworkScheduleOnDate: exception on response parse", e)
			} catch (e: TimeoutCancellationException) {
				Log.e(TAG, "initializeNetworkScheduleOnDate: connection timed-out", e)
				// TODO: show message that couldn't connect to site
			} catch (e: Exception) {
				Log.e(TAG, "initializeNetworkScheduleOnDate: exception", e)
			} finally {
				_uiState.update { it.copy(isLoading = false) }
			}
		}
	
	/**
	 * Check if schedule is remote. Can check [mapOfClasses], [listOfClasses], [schedules].
	 * If no parameters passed than checks current shown [classes][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.classes]
	 *
	 * @param mapOfClasses specific map of [ScheduleWithSubject] to be checked
	 * @param listOfClasses specific list of [ScheduleWithSubject] to be checked
	 * @param schedules specific list of [Schedule] to be checked
	 * @return true if schedule is remote
	 */
	fun isScheduleRemote(
		mapOfClasses: Map<Int, ScheduleWithSubject>? = null,
		listOfClasses: List<ScheduleWithSubject>? = null,
		schedules: List<Schedule>? = null,
	): Boolean {
		val schedulesIds =
			listOfClasses?.map { it.schedule.scheduleId }
				?: mapOfClasses?.map { it.value.schedule.scheduleId }
				?: schedules?.map { it.scheduleId }
				?: uiState.value.classes.map { it.value.schedule.scheduleId }
		return schedulesIds.binarySearch(0L) != -1
	}
	
	private fun currentClassesHasMasterId(): Boolean {
		val schedulesMasterIds = uiState.value.classes.map { it.value.schedule.studyDayMasterId }
		return schedulesMasterIds.binarySearch(null) == -1
	}
	
	/**
	 * Used for adding network schedule to local DB. If classes don't have [studyDayMasterId][com.kxsv.schooldiary.data.local.features.schedule.Schedule.studyDayMasterId]
	 * adds it for each class. Opportunity to determine if schedule should be localized
	 * from [fetched schedule][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.fetchedClasses]
	 *
	 * @throws IllegalStateException when study day is null
	 * @throws IllegalArgumentException when current classes are empty
	 */
	suspend fun localiseNetSchedule(isFetched: Boolean? = false) {
		try {
			val studyDay = getCurrentStudyDayForced()
			
			val classes = if (isFetched!!) uiState.value.fetchedClasses else uiState.value.classes
			if (classes.isNullOrEmpty()) throw IllegalArgumentException("Shouldn't be called with empty classes")
			
			scheduleRepository.deleteAllByDayId(studyDay.studyDayId)
			
			Log.i(TAG, "localiseNetSchedule: in process for ${uiState.value.studyDay}")
			val schedules = classes.map { it.value.schedule }
			
			if (currentClassesHasMasterId()) {
				scheduleRepository.upsertAll(schedules)
			} else {
				val schedulesWithMasterDay =
					schedules.map { it.copy(studyDayMasterId = studyDay.studyDayId) }
				scheduleRepository.upsertAll(schedulesWithMasterDay)
			}
			loadLocalScheduleOnDate(studyDay.date)
		} catch (e: IllegalStateException) {
			Log.e(
				TAG, "localiseNetSchedule: couldn't localise" +
						" because cannot check if schedule is localised", e
			)
		} catch (e: IllegalArgumentException) {
			Log.e(
				TAG,
				"localiseNetSchedule: couldn't localise because nothing to localise", e
			)
		} catch (e: RuntimeException) {
			Log.e(TAG, "localiseNetSchedule: failed to localise net schedule", e)
		}
	}
	
	fun scheduleChoose(scheduleNum: Int) {
		when (scheduleNum) {
			1 -> _uiState.update { it.copy(fetchedClasses = null) }
			2 -> {
				try {
					viewModelScope.launch(ioDispatcher) {
						localiseNetSchedule(true)
						_uiState.update { it.copy(fetchedClasses = null) }
					}
				} catch (e: Exception) {
					Log.e(
						TAG,
						"scheduleChoose(remote): couldn't complete operation successfully",
						e
					)
				}
			}
		}
	}
	
	fun fetchSchedule() {
		_uiState.update { it.copy(isLoading = true) }
		viewModelScope.coroutineContext.job.cancelChildren()
		viewModelScope.launch(ioDispatcher) {
			try {
				if (isScheduleRemote()) {
					localiseNetSchedule()
				} else {
					val fetchedClasses = measurePerformanceInMS(logger = { time, result ->
						Log.i(
							TAG, "fetchSchedule: loadScheduleForDate() performance is $time ms" +
									"\nreturned: classes $result"
						)
					}) {
						withTimeout(10000L) {
							netScheduleDataSource.loadScheduleForDate(uiState.value.selectedDate)
								.toLocalWithSubject()
						}
					}
					if (fetchedClasses.isNotEmpty()) {
						_uiState.update { it.copy(fetchedClasses = fetchedClasses) }
					} else {
						showSnackbarMessage(R.string.absent_net_schedule)
					}
				}
			} catch (e: NetworkException) {
				Log.e(TAG, "fetchSchedule: exception on login", e)
			} catch (e: IOException) {
				Log.e(TAG, "fetchSchedule: exception on response parse", e)
			} catch (e: TimeoutCancellationException) {
				Log.e(TAG, "fetchSchedule: connection timed-out", e)
				// TODO: show message that couldn't connect to site
			} catch (e: Exception) {
				Log.e(TAG, "fetchSchedule: exception", e)
			} finally {
				_uiState.update { it.copy(isLoading = false) }
			}
		}
	}
	
	private fun retrieveScheduleOnDate(date: LocalDate) {
		viewModelScope.coroutineContext.job.cancelChildren()
		viewModelScope.launch(ioDispatcher) {
			val dayWithClasses = measurePerformanceInMS(logger = { time, _ ->
				Log.i(TAG, "getDayAndSchedulesWithSubjectsByDate() performance is $time ms")
			}) {
				studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date)
			}
			if (dayWithClasses != null) {
				_uiState.update { it.copy(studyDay = dayWithClasses.studyDay) }
				if (dayWithClasses.classes.isNotEmpty()) {
					Log.i(TAG, "retrieveScheduleOnDate: found local schedule")
					_uiState.update { dayScheduleUiState ->
						val mappedClasses: MutableMap<Int, ScheduleWithSubject> = mutableMapOf()
						dayWithClasses.classes.forEach { mappedClasses[it.schedule.index] = it }
						dayScheduleUiState.copy(
							classes = mappedClasses,
							isLoading = false
						)
					}
				} else {
					// INFO: loads classes from network to UI with studyDayMasterId
					Log.i(TAG, "retrieveScheduleOnDate: searching for schedule in Net")
					initializeNetworkScheduleOnDate(date)
				}
			} else {
				// INFO: loads classes from network to UI without studyDayMasterId
				Log.i(
					TAG,
					"retrieveScheduleOnDate: studyDay is NULL, searching for schedule in Net"
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
		val appliedPatternId =
			studyDay?.appliedPatternId ?: appSettingsRepository.getPatternId()
		val strokes = measurePerformanceInMS(logger = { time, result ->
			Log.i(
				TAG, "updateTimingsOnStudyDay() performance is $time ms" +
						"\nloaded timings from ${if (isFromDefaults) "default " else ""}pattern(id: $appliedPatternId) ${result.firstOrNull()}..."
			)
		}) {
			strokeRepository.getStrokesByPatternId(appliedPatternId)
		}
		
		_uiState.update {
			it.copy(currentTimings = strokes)
		}
	}
	
	private fun loadLocalScheduleOnDate(date: LocalDate) = viewModelScope.launch(ioDispatcher) {
		studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date).let { dayWithClasses ->
			if (dayWithClasses != null) {
				_uiState.update { dayScheduleUiState ->
					val mappedClasses: MutableMap<Int, ScheduleWithSubject> = mutableMapOf()
					dayWithClasses.classes.forEach { mappedClasses[it.schedule.index] = it }
					dayScheduleUiState.copy(
						studyDay = dayWithClasses.studyDay,
						classes = mappedClasses,
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
	
	private suspend fun List<NetworkSchedule>.toLocalWithSubject(): Map<Int, ScheduleWithSubject> {
		return try {
			val newMap = mutableMapOf<Int, ScheduleWithSubject>()
			this.forEach {
				val localedClass = it.toLocalWithSubject()
				newMap[it.index] = localedClass
			}
			newMap
		} catch (e: RuntimeException) {
			Log.e(
				TAG, "List<NetworkSchedule>.toLocalWithSubject: classes are empty because", e
			)
			emptyMap()
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
	 * Convert [NetworkSchedule] to local, opportunity to push the [studyDayMasterId]
	 * which will be assigned to each [Schedule] after conversion, which is by default
	 * is [current studyDay][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.studyDay]
	 * id or is null.
	 *
	 * @param studyDayMasterId value to assign to each class [studyDayMasterId][com.kxsv.schooldiary.data.local.features.schedule.Schedule.studyDayMasterId] field
	 * @return List<[Schedule]>
	 */
	private suspend fun List<NetworkSchedule>.toLocal(studyDayMasterId: Long? = null): List<Schedule> {
		return try {
			this.map { it.toLocal(studyDayMasterId) }
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
	
	private fun fromTimestamp(value: Long): LocalDate =
		if (value == 0L) LocalDate.now() else {
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