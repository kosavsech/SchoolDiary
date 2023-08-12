package com.kxsv.schooldiary.ui.screens.schedule

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kizitonwose.calendar.core.CalendarDay
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.mapper.toLessonEntities
import com.kxsv.schooldiary.data.mapper.toLessonsWithSubjectIndexed
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.PatternStrokeRepository
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.TimePatternRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.IoDispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.ui.screens.patterns.PatternSelectionResult
import com.kxsv.schooldiary.util.ListExtensionFunctions.copyExclusively
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.measurePerformanceInMS
import com.kxsv.schooldiary.util.Utils.timestampToLocalDate
import com.kxsv.schooldiary.util.Utils.toList
import com.kxsv.schooldiary.util.remote.NetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


private const val TAG = "ScheduleViewModel"

data class DayScheduleUiState(
	val studyDay: StudyDayEntity? = null,
	val classes: Map<Int, LessonWithSubject> = emptyMap(),
	val fetchedClasses: Map<Int, LessonWithSubject>? = null,
	val currentTimings: List<PatternStrokeEntity> = emptyList(),
	val classDetailed: LessonWithSubject? = null,
	val selectedDate: LocalDate = Utils.currentDate, // unique for DayScheduleScreen
	val selectedRefCalendarDay: CalendarDay? = null, // unique for DayScheduleCopyScreen
	val refRange: ClosedRange<LocalDate>? = null,
	val destRange: ClosedRange<LocalDate>? = null,
	val userMessage: Int? = null,
	val userMessageArgs: Array<out Any>? = null,
	val isLoading: Boolean = false,
)

@HiltViewModel
class ScheduleViewModel @Inject constructor(
	private val lessonRepository: LessonRepository,
	private val subjectRepository: SubjectRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val studyDayRepository: StudyDayRepository,
	private val patternRepository: TimePatternRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val navArgs: DayScheduleScreenNavArgs = savedStateHandle.navArgs()
	private val dateStamp: Long? = navArgs.datestamp
	private val showComparison: Boolean? = navArgs.showComparison
	
	private val _uiState = MutableStateFlow(DayScheduleUiState())
	val uiState: StateFlow<DayScheduleUiState> = _uiState.asStateFlow()
	
	private var scheduleRetrieveJob: Job? = null
	private var scheduleFetchJob: Job? = null
	
	init {
		onDayChangeUpdate(date = timestampToLocalDate(dateStamp) ?: Utils.currentDate)
		if (showComparison == true) fetchSchedule()
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null, userMessageArgs = null)
		}
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_schedule_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_schedule_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_schedule_message)
		}
	}
	
	private fun showCustomPatternMessage(setPatternName: String) {
		showSnackbarMessage(R.string.successfully_set_custom_pattern)
		setSnackbarArgs(setPatternName)
	}
	
	fun showDayScheduleCopyResult(result: DayScheduleCopyResult) {
		val text = if (!result.isTimingsCopied) {
			R.string.successfully_copied_day_schedule_0
		} else {
			R.string.successfully_copied_day_schedule_1
		}
		showSnackbarMessage(text)
		setSnackbarArgs(result.fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
	}
	
	fun showDateRangeScheduleCopyResult(result: DateRangeScheduleCopyResult) {
		val text = if (!result.isTimingsCopied) {
			R.string.successfully_copied_date_range_schedule_0
		} else {
			R.string.successfully_copied_date_range_schedule_1
		}
		showSnackbarMessage(text)
		setSnackbarArgs(
			result.fromRangeStart.format(DateTimeFormatter.ISO_LOCAL_DATE),
			result.fromRangeEnd.format(DateTimeFormatter.ISO_LOCAL_DATE),
			result.toRangeStart.format(DateTimeFormatter.ISO_LOCAL_DATE),
			result.toRangeStart.plusDays(result.fromRangeStart.until(result.fromRangeEnd).days.toLong()),
		)
	}
	
	fun selectCustomPattern(result: PatternSelectionResult) {
		showCustomPatternMessage(result.patternName)
		viewModelScope.launch(ioDispatcher) {
			val currentStudyDay = uiState.value.studyDay?.let {
				val updatedCurrentStudyDay = it.copy(appliedPatternId = result.patternId)
				studyDayRepository.update(updatedCurrentStudyDay)
				return@let updatedCurrentStudyDay
			} ?: createNewStudyDay(predefinedPatternId = result.patternId)
			updateTimingsByStudyDay(currentStudyDay)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	private fun setSnackbarArgs(vararg args: Any) {
		_uiState.update { it.copy(userMessageArgs = args) }
	}
	
	fun deleteClass(lesson: LessonWithSubject) {
		val newClasses = copyExclusively(
			targetItemKey = lesson.lesson.index,
			elements = uiState.value.classes
		)
		_uiState.update {
			it.copy(
				classDetailed = null, classes = newClasses
			)
		}
		try {
			viewModelScope.launch(ioDispatcher) {
				if (lesson.lesson.lessonId == 0L) {
					Log.i(
						TAG, "deleteClass: deleting class w/o scheduleId.\n" +
								"In other words, class from network schedule."
					)
					localiseCachedNetClasses()
				}
				lessonRepository.deleteLesson(lesson.lesson.lessonId)
				showEditResultMessage(DELETE_RESULT_OK)
			}
		} catch (e: Exception) {
			Log.e(TAG, "deleteClass: couldn't delete class from DB", e)
		}
	}
	
	fun selectRefRange(startDate: LocalDate, endDate: LocalDate) {
		_uiState.update { it.copy(refRange = startDate..endDate) }
	}
	
	fun selectDestRange(startDate: LocalDate, endDate: LocalDate) {
		_uiState.update { it.copy(destRange = startDate..endDate) }
	}
	
	fun selectClass(lesson: LessonWithSubject) {
		_uiState.update { it.copy(classDetailed = lesson) }
	}
	
	fun unselectClass() {
		_uiState.update { it.copy(classDetailed = null) }
	}
	
	/**
	 * Create new study day for [date]
	 *
	 * @param date by default is current date
	 * @param predefinedPatternId
	 * @throws RuntimeException if couldn't create study day
	 */
	private suspend fun createNewStudyDay(
		date: LocalDate = uiState.value.selectedDate,
		predefinedPatternId: Long? = null,
	): StudyDayEntity {
		var appliedPatternId = predefinedPatternId
		if (predefinedPatternId == null) {
			// INFO: try to get default one if fails then remains null
			val defaultPatternId = userPreferencesRepository.getPatternId()
			val defaultPattern = patternRepository.getPattern(defaultPatternId)
			
			if (defaultPattern != null && defaultPatternId != 0L) {
				appliedPatternId = defaultPatternId
			}
		}
		
		val newStudyDay = StudyDayEntity(
			date = date,
			appliedPatternId = appliedPatternId,
		)
		val newStudyDayId = studyDayRepository.create(newStudyDay)
		if (newStudyDayId == 0L) throw RuntimeException("StudyDayEntity wasn't created for some reason")
		_uiState.update {
			it.copy(studyDay = newStudyDay.copy(studyDayId = newStudyDayId))
		}
		return uiState.value.studyDay!!
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
				copyCachedNetScheduleToCurrentDay(shouldCopyPattern = shouldCopyPattern)
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
	
	fun copyScheduleToRange(shouldCopyPattern: Boolean) {
		if (uiState.value.refRange == null || uiState.value.destRange == null)
			throw RuntimeException("copyScheduleToRange() was called but refRange is null.")
		_uiState.update { it.copy(isLoading = true) }
		
		val copyFromDays = uiState.value.refRange!!.toList()
		// limiter to stop copying, if copyFromDays is ended
		val copyToDays = uiState.value.destRange!!.toList(limiter = copyFromDays.size.toLong())
		
		for ((index, toDate) in copyToDays.withIndex()) {
			viewModelScope.launch(ioDispatcher) {
				val fromDate = copyFromDays[index]
				Log.d(
					TAG, "copyScheduleToRange() called with:" +
							" dateCopyFrom = ${copyFromDays[index]}, toDate = $toDate"
				)
				viewModelScope.launch(ioDispatcher) {
					if (isScheduleRemoteOnDate(fromDate)) {
						Log.d(
							TAG,
							"copyScheduleToRange() isScheduleRemoteOnDate = true (fromDate = $fromDate)"
						)
						copyNetSchedule(
							fromDate = fromDate,
							toDate = toDate,
							shouldCopyPattern = shouldCopyPattern
						)
					} else {
						Log.d(
							TAG,
							"copyScheduleToRange() isScheduleRemoteOnDate = false (fromDate = $fromDate)"
						)
						copyLocalScheduleFromDate(
							fromDate = fromDate,
							toDate = toDate,
							shouldCopyPattern = shouldCopyPattern
						)
					}
				}
				
			}
		}
		_uiState.update { it.copy(refRange = null, destRange = null) }
	}
	
	fun onCalendarDayChangeUpdate(calendarDay: CalendarDay?) {
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
			Log.i(TAG, "onCalendarDayChangeUpdate() loading classes from $calendarDay.")
			retrieveScheduleOnDate(calendarDay.date)
		} else {
			Log.d(
				TAG,
				"onCalendarDayChangeUpdate: couldn't load classes because calendar day is null."
			)
		}
	}
	
	fun onDayChangeUpdate(date: LocalDate?) {
		_uiState.update { it.copy(isLoading = true, classes = emptyMap(), fetchedClasses = null) }
		if (date != null) _uiState.update { it.copy(selectedDate = date) }
		retrieveScheduleOnDate(date = uiState.value.selectedDate)
	}
	
	/**
	 * Localises current schedule from net which is cached, and returns id for lesson that will be edited
	 *
	 * @return id of lesson that being edit
	 */
	suspend fun getIdForClassFromNet(): Long? {
		localiseCachedNetClasses()
		val indexOfClassDetailed = uiState.value.classDetailed!!.lesson.index
		val studyDayMasterId = uiState.value.studyDay!!.studyDayId
		
		val result =
			lessonRepository.getByIdAndIndex(
				studyDayMasterId = studyDayMasterId,
				index = indexOfClassDetailed
			)?.lessonId
		if (result == null) showSnackbarMessage(R.string.lesson_not_found)
		return result
	}
	
	/**
	 * Copies lessons from [specific day][fromDate] to [another day][toDate].
	 * Does DB call, so should be time effective.
	 *
	 * @param fromDate any date
	 * @param toDate any date
	 * @param shouldCopyPattern determines whether the method will copy applied pattern id from
	 * reference study day
	 * @see copyCachedNetScheduleToCurrentDay
	 * @see copyNetSchedule
	 */
	private suspend fun copyLocalScheduleFromDate(
		fromDate: LocalDate, toDate: LocalDate, shouldCopyPattern: Boolean,
	) {
		try {
			val refStudyDay = studyDayRepository.getByDate(fromDate)
				?: throw NoSuchElementException("Reference StudyDayEntity (date $fromDate) to copy from not found.")
			val toStudyDay = studyDayRepository.getByDate(toDate)
			
			val cloneDay = if (toStudyDay != null) {
				// INFO: if copying lesson to existent study day,
				//  maybe with local lessons which should be deleted first.
				var updatedToStudyDay = toStudyDay
				if (shouldCopyPattern) {
					updatedToStudyDay =
						toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
					studyDayRepository.update(updatedToStudyDay)
				}
				lessonRepository.deleteAllByDayId(toStudyDay.studyDayId)
				updatedToStudyDay
			} else {
				if (shouldCopyPattern) {
					createNewStudyDay(
						date = toDate,
						predefinedPatternId = refStudyDay.appliedPatternId
					)
				} else {
					createNewStudyDay(date = toDate)
				}
			}
			Log.d(
				TAG,
				"copyLocalScheduleFromDate() called with toStudyDay $toStudyDay\n" +
						"fromStudyDay $refStudyDay"
			)
			val copyOfLessons: MutableList<LessonEntity> = mutableListOf()
			lessonRepository.getAllByMasterId(refStudyDay.studyDayId).forEach {
				copyOfLessons.add(it.copy(studyDayMasterId = cloneDay.studyDayId, lessonId = 0))
			}
			lessonRepository.upsertAll(copyOfLessons)
			
			loadLocalScheduleOnDate(toDate)
			updateTimingsByStudyDay(cloneDay)
		} catch (e: NoSuchElementException) {
			Log.e(
				TAG, "copyLocalScheduleFromDate: couldn't copy lesson because nowhere from to", e
			)
		}
	}
	
	/**
	 * Copies remote lessons from [given day][fromDate] to [destination day][toDate].
	 * Does one network call to obtain remote lessons on [given day][fromDate]
	 * from [edu.tatar.ru](https://edu.tatar.ru/), so it takes some time(~1 sec).
	 *
	 * @param fromDate any date
	 * @param toDate any date
	 * @param shouldCopyPattern determines whether the method will copy applied pattern id from
	 * reference study day
	 * @see copyCachedNetScheduleToCurrentDay
	 * @throws NetworkException.NotLoggedInException
	 */
	private suspend fun copyNetSchedule(
		fromDate: LocalDate, toDate: LocalDate, shouldCopyPattern: Boolean,
	) {
		try {
			val refStudyDay = studyDayRepository.getByDate(fromDate)
			val toStudyDay = studyDayRepository.getByDate(toDate)
			
			val cloneDay = if (toStudyDay != null) {
				// INFO: if copying lesson to existent study day,
				//  maybe with local lessons which should be deleted first.
				lessonRepository.deleteAllByDayId(toStudyDay.studyDayId)
				var updatedToStudyDay = toStudyDay
				if (shouldCopyPattern && refStudyDay != null) {
					updatedToStudyDay =
						toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
					studyDayRepository.update(updatedToStudyDay)
				}
				updatedToStudyDay
			} else {
				if (shouldCopyPattern && refStudyDay != null) {
					createNewStudyDay(
						date = toDate,
						predefinedPatternId = refStudyDay.appliedPatternId
					)
				} else {
					createNewStudyDay(date = toDate)
				}
			}
			Log.w(
				TAG,
				"copyNetSchedule: uiState.value.classes = ${uiState.value.classes}"
			)
			val localizedNetClasses =
				lessonRepository.fetchLessonsOnDate(fromDate)
					.toLessonEntities(
						overrideStudyDayMasterId = cloneDay.studyDayId,
						subjectRepository = subjectRepository,
						studyDayRepository = studyDayRepository
					)
			
			lessonRepository.upsertAll(localizedNetClasses)
			
			loadLocalScheduleOnDate(toDate)
			updateTimingsByStudyDay(cloneDay)
		} catch (e: NetworkException) {
			Log.e(TAG, "copyNetSchedule: exception on login", e)
		} catch (e: IOException) {
			Log.e(TAG, "copyNetSchedule: exception on response parseTerm", e)
		} catch (e: Exception) {
			Log.e(TAG, "copyNetSchedule: exception", e)
		} catch (e: Exception) {
			Log.e(TAG, "copyNetSchedule: caught", e)
		}
	}
	
	/**
	 * Copies remote lessons from [selected reference day][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.selectedRefCalendarDay] which are cached in [classes][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.classes]
	 * to show lessons preview in [DayScheduleCopyScreen] for [current day][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.selectedDate].
	 * Does not make network calls, so time efficient.
	 *
	 * @param shouldCopyPattern determines whether the method will copy applied pattern id from
	 * reference study day
	 * @see copyNetSchedule
	 */
	private suspend fun copyCachedNetScheduleToCurrentDay(shouldCopyPattern: Boolean) {
		try {
			val refStudyDay =
				studyDayRepository.getByDate(uiState.value.selectedRefCalendarDay!!.date)
			val toStudyDay = studyDayRepository.getByDate(uiState.value.selectedDate)
			
			val cloneDay = if (toStudyDay != null) {
				// INFO: if copying lesson to existent study day,
				//  maybe with local lessons which should be deleted first.
				lessonRepository.deleteAllByDayId(toStudyDay.studyDayId)
				var updatedCloneStudyDay = toStudyDay
				if (shouldCopyPattern && refStudyDay != null) {
					updatedCloneStudyDay =
						toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
					studyDayRepository.update(updatedCloneStudyDay)
				}
				updatedCloneStudyDay
			} else {
				if (shouldCopyPattern && refStudyDay != null) {
					createNewStudyDay(
						date = uiState.value.selectedDate,
						predefinedPatternId = refStudyDay.appliedPatternId
					)
				} else {
					createNewStudyDay(date = uiState.value.selectedDate)
				}
			}
			
			val localizedNetClasses =
				uiState.value.classes.map { it.value.lesson.copy(studyDayMasterId = cloneDay.studyDayId) }
			
			lessonRepository.upsertAll(localizedNetClasses)
			
			loadLocalScheduleOnDate(uiState.value.selectedDate)
			updateTimingsByStudyDay(cloneDay)
		} catch (e: Exception) {
			Log.e(TAG, "copyNetSchedule: caught", e)
		}
	}
	
	private suspend fun initializeNetworkScheduleOnDate(date: LocalDate) {
		try {
			val classes = measurePerformanceInMS(logger = { time, result ->
				Log.i(
					TAG,
					"initializeNetworkScheduleOnDate: getDayInfo() performance is $time ms" +
							"\nreturned: classes $result"
				)
			}) {
				withTimeout(10000L) {
					lessonRepository.fetchLessonsOnDate(date)
						.toLessonsWithSubjectIndexed(subjectRepository, studyDayRepository)
				}
			}
			_uiState.update { it.copy(classes = classes) }
		} catch (e: NetworkException) {
			Log.e(TAG, "initializeNetworkScheduleOnDate: exception on login", e)
		} catch (e: IOException) {
			Log.e(TAG, "initializeNetworkScheduleOnDate: exception on response parseTerm", e)
		} catch (e: TimeoutCancellationException) {
			// TODO: show message that couldn't connect to site
			Log.e(TAG, "initializeNetworkScheduleOnDate: connection timed-out", e)
		} catch (e: CancellationException) {
			Log.w(TAG, "initializeNetworkScheduleOnDate: canceled", e)
		} catch (e: Exception) {
			Log.e(TAG, "initializeNetworkScheduleOnDate: exception", e)
		} finally {
			_uiState.update { it.copy(isLoading = false) }
		}
	}
	
	/**
	 * If no parameters passed then checks current shown [classes][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.classes]
	 * Can check [mapOfClasses], [listOfClasses], [lessons].
	 *
	 * @param mapOfClasses specific map of [LessonWithSubject] to be checked
	 * @param listOfClasses specific list of [LessonWithSubject] to be checked
	 * @param lessons specific list of [LessonEntity] to be checked
	 * @return true if lesson is remote
	 */
	fun isScheduleRemote(
		lessons: List<LessonEntity>? = null,
		mapOfClasses: Map<Int, LessonWithSubject>? = null,
		listOfClasses: List<LessonWithSubject>? = null,
	): Boolean {
		val schedulesIds =
			listOfClasses?.map { it.lesson.lessonId }
				?: mapOfClasses?.map { it.value.lesson.lessonId }
				?: lessons?.map { it.lessonId }
				?: uiState.value.classes.map { it.value.lesson.lessonId }
		return schedulesIds.binarySearch(0L) != -1
	}
	
	private fun doHaveMasterId(classes: Map<Int, LessonWithSubject>): Boolean {
		val schedulesMasterIds = classes.map { it.value.lesson.studyDayMasterId }
		return schedulesMasterIds.binarySearch(null) == -1
	}
	
	/**
	 * Used for saving cached network classes on [current day][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.selectedDate]
	 * to local DB.
	 * If classes don't have [studyDayMasterId][com.kxsv.schooldiary.data.local.features.lesson.LessonEntity.studyDayMasterId]
	 * adds it for each class. Added studyDayMasterId equals to id of [current study day][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.studyDay].
	 *
	 *
	 * @param isFetched determines if lessons should be localized from [fetched lesson][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.fetchedClasses]
	 * @throws IllegalStateException when study day is null
	 * @throws IllegalArgumentException when current classes are empty
	 */
	suspend fun localiseCachedNetClasses(isFetched: Boolean = false) {
		measurePerformanceInMS(logger = { time, _ ->
			Log.i(TAG, "localiseCachedNetClasses: performance is $time ms")
		}) {
			try {
				val studyDay = uiState.value.studyDay ?: createNewStudyDay()
				val classes = if (isFetched) uiState.value.fetchedClasses else uiState.value.classes
				
				if (classes.isNullOrEmpty())
					throw IllegalArgumentException(
						"localiseCachedNetClasses(isFetched = $isFetched) " +
								"was called but classes are ${classes?.isEmpty()} which shouldn't be the case"
					)
				Log.i(TAG, "localiseCachedNetClasses: in process for ${uiState.value.studyDay}")
				
				lessonRepository.deleteAllByDayId(studyDay.studyDayId)
				
				val lessons = classes.map { it.value.lesson }
				
				if (isFetched || !doHaveMasterId(classes = classes)) {
					val lessonsWithMasterDay = lessons.map { lesson ->
						lesson.copy(studyDayMasterId = studyDay.studyDayId)
					}
					lessonRepository.upsertAll(lessonsWithMasterDay)
				} else {
					lessonRepository.upsertAll(lessons)
				}
				if (isFetched) {
					_uiState.update { it.copy(classes = classes) }
				} else {
				
				}
			} catch (e: IllegalArgumentException) {
				Log.e(TAG, "localiseCachedNetClasses: couldn't localise because nothing to: ", e)
			} catch (e: RuntimeException) {
				Log.e(TAG, "localiseCachedNetClasses: failed to localise net lesson: ", e)
			}
		}
	}
	
	fun scheduleChoose(scheduleNum: Int) {
		when (scheduleNum) {
			1 -> _uiState.update { it.copy(fetchedClasses = null) }
			2 -> {
				try {
					viewModelScope.launch(ioDispatcher) {
						localiseCachedNetClasses(true)
						_uiState.update { it.copy(fetchedClasses = null) }
					}
				} catch (e: Exception) {
					Log.e(
						TAG,
						"scheduleChoose(remote): couldn't complete operation successfully", e
					)
				}
			}
		}
	}
	
	/**
	 * @throws NetworkException.NotLoggedInException
	 */
	fun fetchSchedule() {
		_uiState.update { it.copy(isLoading = true) }
		scheduleFetchJob?.cancel()
		scheduleFetchJob = viewModelScope.launch(ioDispatcher) {
			try {
				val fetchedClasses = measurePerformanceInMS(logger = { time, result ->
					Log.i(
						TAG, "fetchSchedule: getDayInfo() performance is $time ms" +
								"\nreturned: classes $result"
					)
				}) {
					withTimeout(10000L) {
						lessonRepository.fetchLessonsOnDate(uiState.value.selectedDate)
							.toLessonsWithSubjectIndexed(subjectRepository, studyDayRepository)
					}
				}
				if (fetchedClasses.isNotEmpty()) {
					_uiState.update { it.copy(fetchedClasses = fetchedClasses) }
				} else {
					showSnackbarMessage(R.string.absent_net_schedule)
				}
			} catch (e: NetworkException) {
				Log.e(TAG, "fetchSchedule: exception on login", e)
			} catch (e: IOException) {
				Log.e(TAG, "fetchSchedule: exception on response parseTerm", e)
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
		scheduleRetrieveJob?.cancel()
		scheduleRetrieveJob = viewModelScope.launch(ioDispatcher) {
			val dayWithClasses = measurePerformanceInMS(logger = { time, _ ->
				Log.i(TAG, "getLocalSchedule() performance is $time ms")
			}) {
				studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date)
			}
			_uiState.update { it.copy(studyDay = dayWithClasses?.studyDay) }
			if (dayWithClasses != null) {
				if (dayWithClasses.classes.isNotEmpty()) {
					Log.i(TAG, "retrieveScheduleOnDate: found local classes")
					val mappedClasses = mutableMapOf<Int, LessonWithSubject>()
					dayWithClasses.classes.forEach { mappedClasses[it.lesson.index] = it }
					_uiState.update {
						it.copy(classes = mappedClasses, isLoading = false)
					}
				} else {
					Log.i(TAG, "retrieveSchedule: loaded studyDay. But searching for lesson in Net")
					initializeNetworkScheduleOnDate(date)
				}
			} else {
				Log.i(TAG, "retrieveSchedule: NULL studyDay. Searching for lesson in Net")
				initializeNetworkScheduleOnDate(date)
			}
			updateTimingsByStudyDay(dayWithClasses?.studyDay)
		}
	}
	
	private suspend fun isScheduleRemoteOnDate(date: LocalDate): Boolean {
		val dayWithClasses = studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date)
		return dayWithClasses?.classes?.isEmpty() ?: true
	}
	
	/**
	 * Tries to load time pattern from passed [studyDay] if it's null or doesn't
	 * have time pattern specified, loads default pattern from [user_preferences][com.kxsv.schooldiary.data.local.user_preferences.UserPreferences]
	 *
	 * @param studyDay
	 */
	private suspend fun updateTimingsByStudyDay(studyDay: StudyDayEntity?) {
		val isFromDefaults = studyDay?.appliedPatternId == null
		val appliedPatternId =
			studyDay?.appliedPatternId ?: userPreferencesRepository.getPatternId()
		val strokes = measurePerformanceInMS(logger = { time, result ->
			Log.i(
				TAG, "updateTimingsByStudyDay() performance is $time ms" +
						"\nloaded timings from ${if (isFromDefaults) "default " else ""}pattern(id: $appliedPatternId) ${result.firstOrNull()}..."
			)
		}) {
			strokeRepository.getStrokesByPatternId(appliedPatternId)
		}
		
		_uiState.update {
			it.copy(currentTimings = strokes)
		}
	}
	
	fun loadLocalScheduleOnDate(date: LocalDate) = viewModelScope.launch(ioDispatcher) {
		studyDayRepository.getDayAndSchedulesWithSubjectsByDate(date).let { dayWithClasses ->
			if (dayWithClasses != null) {
				_uiState.update {
					val mappedClasses: MutableMap<Int, LessonWithSubject> = mutableMapOf()
					dayWithClasses.classes.forEach { lessonWithSubject ->
						mappedClasses[lessonWithSubject.lesson.index] = lessonWithSubject
					}
					it.copy(
						studyDay = dayWithClasses.studyDay,
						classes = mappedClasses,
						isLoading = false
					)
				}
			} else {
				throw NoSuchElementException("Didn't found day with classes for some reason")
			}
		}
	}
	
}