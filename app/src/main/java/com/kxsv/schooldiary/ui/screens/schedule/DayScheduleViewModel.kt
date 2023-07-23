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
import com.kxsv.schooldiary.data.mapper.toLocalWithSubject
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.PatternStrokeRepository
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.TimePatternRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.SELECTED_CUSTOM_PATTERN_OK
import com.kxsv.schooldiary.util.ListExtensionFunctions.copyExclusively
import com.kxsv.schooldiary.util.Utils.timestampToLocalDate
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
import java.time.temporal.ChronoUnit
import javax.inject.Inject


private const val TAG = "DayScheduleViewModel"

data class DayScheduleUiState(
	val studyDay: StudyDayEntity? = null,
	val classes: Map<Int, LessonWithSubject> = emptyMap(),
	val fetchedClasses: Map<Int, LessonWithSubject>? = null,
	val currentTimings: List<PatternStrokeEntity> = emptyList(),
	val classDetailed: LessonWithSubject? = null,
	val selectedDate: LocalDate = LocalDate.now(), // unique for DayScheduleScreen
	val selectedRefCalendarDay: CalendarDay? = null, // unique for DayScheduleCopyScreen
	val refRange: ClosedRange<LocalDate>? = null,
	val destRange: ClosedRange<LocalDate>? = null,
	val userMessage: Int? = null,
	val isLoading: Boolean = false,
)

@HiltViewModel
class DayScheduleViewModel @Inject constructor(
	private val lessonRepository: LessonRepository,
	private val subjectRepository: SubjectRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val studyDayRepository: StudyDayRepository,
	private val patternRepository: TimePatternRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val dateStamp: Long? = savedStateHandle[AppDestinationsArgs.DATESTAMP_ARG]
	
	private val _uiState = MutableStateFlow(DayScheduleUiState())
	val uiState: StateFlow<DayScheduleUiState> = _uiState.asStateFlow()
	
	private var scheduleFetchJob: Job? = null
	private var scheduleRetrieveJob: Job? = null
	
	init {
		onDayChangeUpdate(timestampToLocalDate(dateStamp) ?: LocalDate.now())
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
					Log.i(TAG, "deleteClass: tried to delete class w/o scheduleId.")
					localiseNetSchedule()
				}
				lessonRepository.deleteLesson(lesson.lesson.lessonId)
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
	
	fun selectClass(lesson: LessonWithSubject) {
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
	
	fun onDayChangeUpdate(date: LocalDate) {
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
		
		val indexOfClassDetailed = uiState.value.classDetailed!!.lesson.index
		val studyDayMasterId = uiState.value.studyDay!!.studyDayId
		return lessonRepository.getByIdAndIndex(
			studyDayMasterId = studyDayMasterId,
			index = indexOfClassDetailed
		)?.lessonId
			?: throw NoSuchElementException("Didn't find lesson with id($studyDayMasterId) and index($indexOfClassDetailed)")
	}
	
	/**
	 * Copies remote lesson from [specific day][fromDate] to [another day][toDate].
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
				?: throw NoSuchElementException("Reference StudyDayEntity (date $fromDate) to copy from not found.")
			val toStudyDay = studyDayRepository.getByDate(toDate)
			
			val cloneDayId = if (toStudyDay != null) {
				// INFO: if copying lesson to existent study day,
				//  maybe with local lessons which should be deleted first.
				if (shouldCopyPattern) {
					studyDayRepository.update(
						toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
					)
				}
				lessonRepository.deleteAllByDayId(toStudyDay.studyDayId)
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
			val copyOfLessons: MutableList<LessonEntity> = mutableListOf()
			lessonRepository.getAllByMasterId(refStudyDay.studyDayId).forEach {
				copyOfLessons.add(it.copy(studyDayMasterId = cloneDayId, lessonId = 0))
			}
			lessonRepository.upsertAll(copyOfLessons)
			
			loadLocalScheduleOnDate(toDate)
		} catch (e: NoSuchElementException) {
			Log.e(
				TAG, "copyLocalScheduleFromDate: couldn't copy lesson because nowhere from to", e
			)
		}
	}
	
	/**
	 * Copies remote lesson from [specific day][fromDate] to [another day][toDate].
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
				// INFO: if copying lesson to existent study day,
				//  maybe with local lessons which should be deleted first.
				if (shouldCopyPattern && refStudyDay != null) {
					studyDayRepository.update(
						toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
					)
				}
				lessonRepository.deleteAllByDayId(toStudyDay.studyDayId)
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
				lessonRepository.fetchLessonsByDate(fromDate)
					.toLessonEntities(cloneDayId, subjectRepository, studyDayRepository)
			
			lessonRepository.upsertAll(localizedNetClasses)
			
			loadLocalScheduleOnDate(toDate)
		} catch (e: NetworkException) {
			Log.e(TAG, "copyRemoteSchedule: exception on login", e)
		} catch (e: IOException) {
			Log.e(TAG, "copyRemoteSchedule: exception on response parseTerm", e)
		} catch (e: Exception) {
			Log.e(TAG, "copyRemoteSchedule: exception", e)
		} catch (e: Exception) {
			Log.e(TAG, "copyRemoteSchedule: caught", e)
		}
	}
	
	/**
	 * Copies remote lesson from [specific day][fromDate] to [current day][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.selectedDate].
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
				// INFO: if copying lesson to existent study day,
				//  maybe with local lessons which should be deleted first.
				if (shouldCopyPattern && refStudyDay != null) {
					studyDayRepository.update(
						toStudyDay.copy(appliedPatternId = refStudyDay.appliedPatternId)
					)
				}
				lessonRepository.deleteAllByDayId(toStudyDay.studyDayId)
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
				uiState.value.classes.map { it.value.lesson.copy(studyDayMasterId = cloneDayId) }
			
			lessonRepository.upsertAll(localizedNetClasses)
			
			loadLocalScheduleOnDate(uiState.value.selectedDate)
		} catch (e: Exception) {
			Log.e(TAG, "copyRemoteSchedule: caught", e)
		}
	}
	
	/**
	 * Does whatever it takes to get study day by [date]. If doesn't exist, [creates new][createNewStudyDay].
	 *
	 * @param date
	 * @return [StudyDayEntity]
	 */
	suspend fun getStudyDayForced(date: LocalDate): StudyDayEntity {
		val existingStudyDay = studyDayRepository.getByDate(date)
		val studyDay = (if (existingStudyDay != null) {
			existingStudyDay
		} else {
			createNewStudyDay()
			uiState.value.studyDay
		})
			?: throw IllegalStateException("StudyDayEntity is still null somehow")
		return studyDay
	}
	
	/**
	 * Does whatever it takes to get study day for current [date][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.selectedDate].
	 * If doesn't exist [creates new][createNewStudyDay].
	 *
	 * @return [StudyDayEntity]
	 * @throws IllegalStateException if didn't succeed to create newStudyDay
	 */
	suspend fun getCurrentStudyDayForced(): StudyDayEntity {
//		userPreferencesRepository.setAuthCookie(null)
		uiState.value.studyDay ?: createNewStudyDay()
		return uiState.value.studyDay
			?: throw IllegalStateException("StudyDayEntity is still null somehow")
	}
	
	private suspend fun initializeNetworkScheduleOnDate(date: LocalDate) {
		try {
			val classes = measurePerformanceInMS(logger = { time, result ->
				Log.i(
					TAG,
					"initializeNetworkScheduleOnDate: getScheduleForDate() performance is $time ms" +
							"\nreturned: classes $result"
				)
			}) {
				withTimeout(10000L) {
					lessonRepository.fetchLessonsByDate(date)
						.toLocalWithSubject(subjectRepository, studyDayRepository)
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
	 * If no parameters passed than checks current shown [classes][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.classes]
	 * Can check [mapOfClasses], [listOfClasses], [lessons].
	 *
	 * @param mapOfClasses specific map of [LessonWithSubject] to be checked
	 * @param listOfClasses specific list of [LessonWithSubject] to be checked
	 * @param lessons specific list of [LessonEntity] to be checked
	 * @return true if lesson is remote
	 */
	fun isScheduleRemote(
		mapOfClasses: Map<Int, LessonWithSubject>? = null,
		listOfClasses: List<LessonWithSubject>? = null,
		lessons: List<LessonEntity>? = null,
	): Boolean {
		val schedulesIds =
			listOfClasses?.map { it.lesson.lessonId }
				?: mapOfClasses?.map { it.value.lesson.lessonId }
				?: lessons?.map { it.lessonId }
				?: uiState.value.classes.map { it.value.lesson.lessonId }
		return schedulesIds.binarySearch(0L) != -1
	}
	
	private fun currentClassesHasMasterId(): Boolean {
		val schedulesMasterIds = uiState.value.classes.map { it.value.lesson.studyDayMasterId }
		return schedulesMasterIds.binarySearch(null) == -1
	}
	
	/**
	 * Used for adding network lesson to local DB. If classes don't have [studyDayMasterId][com.kxsv.schooldiary.data.local.features.lesson.LessonEntity.studyDayMasterId]
	 * adds it for each class. Opportunity to determine if lesson should be localized
	 * from [fetched lesson][com.kxsv.schooldiary.ui.screens.schedule.DayScheduleUiState.fetchedClasses]
	 *
	 * @throws IllegalStateException when study day is null
	 * @throws IllegalArgumentException when current classes are empty
	 */
	suspend fun localiseNetSchedule(isFetched: Boolean? = false) {
		measurePerformanceInMS(logger = { time, _ ->
			Log.i(TAG, "localiseNetSchedule: performance is $time ms")
		}) {
			try {
				val studyDay = getCurrentStudyDayForced()
				
				val classes =
					if (isFetched!!) uiState.value.fetchedClasses else uiState.value.classes
				if (classes.isNullOrEmpty()) throw IllegalArgumentException("Shouldn't be called with empty classes")
				
				lessonRepository.deleteAllByDayId(studyDay.studyDayId)
				
				Log.i(TAG, "localiseNetSchedule: in process for ${uiState.value.studyDay}")
				val schedules = classes.map { it.value.lesson }
				
				if (currentClassesHasMasterId()) {
					lessonRepository.upsertAll(schedules)
				} else {
					val schedulesWithMasterDay =
						schedules.map { it.copy(studyDayMasterId = studyDay.studyDayId) }
					lessonRepository.upsertAll(schedulesWithMasterDay)
				}
				loadLocalScheduleOnDate(studyDay.date)
			} catch (e: IllegalStateException) {
				Log.e(
					TAG, "localiseNetSchedule: couldn't localise" +
							" because cannot check if lesson is localised", e
				)
			} catch (e: IllegalArgumentException) {
				Log.e(
					TAG,
					"localiseNetSchedule: couldn't localise because nothing to localise", e
				)
			} catch (e: RuntimeException) {
				Log.e(TAG, "localiseNetSchedule: failed to localise net lesson", e)
			}
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
		scheduleFetchJob?.cancel()
		scheduleFetchJob = viewModelScope.launch(ioDispatcher) {
			try {
				if (isScheduleRemote()) {
					localiseNetSchedule()
				} else {
					val fetchedClasses = measurePerformanceInMS(logger = { time, result ->
						Log.i(
							TAG, "fetchSchedule: getScheduleForDate() performance is $time ms" +
									"\nreturned: classes $result"
						)
					}) {
						withTimeout(10000L) {
							lessonRepository.fetchLessonsByDate(uiState.value.selectedDate)
								.toLocalWithSubject(subjectRepository, studyDayRepository)
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
			if (dayWithClasses != null) {
				_uiState.update { it.copy(studyDay = dayWithClasses.studyDay) }
				if (dayWithClasses.classes.isNotEmpty()) {
					Log.i(TAG, "retrieveScheduleOnDate: found local classes")
					val mappedClasses = mutableMapOf<Int, LessonWithSubject>()
					dayWithClasses.classes.forEach { mappedClasses[it.lesson.index] = it }
					_uiState.update {
						it.copy(classes = mappedClasses, isLoading = false)
					}
				} else {
					Log.i(TAG, "retrieveSchedule: loaded studyDay, searching for lesson in Net")
					initializeNetworkScheduleOnDate(date)
				}
			} else {
				Log.i(TAG, "retrieveSchedule: studyDay is NULL, searching for lesson in Net")
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
	 * have time pattern specified, loads default pattern from [user_preferences][com.kxsv.schooldiary.data.local.user_preferences.UserPreferences]
	 *
	 * @param studyDay
	 */
	private suspend fun updateTimingsOnStudyDay(studyDay: StudyDayEntity?) {
		val isFromDefaults = studyDay?.appliedPatternId == null
		val appliedPatternId =
			studyDay?.appliedPatternId ?: userPreferencesRepository.getPatternId()
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
					val mappedClasses: MutableMap<Int, LessonWithSubject> = mutableMapOf()
					dayWithClasses.classes.forEach { mappedClasses[it.lesson.index] = it }
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
	
	//the inline performance measurement method
	private inline fun <T> measurePerformanceInMS(logger: (Long, T) -> Unit, func: () -> T): T {
		val startTime = System.currentTimeMillis()
		val result: T = func.invoke()
		val endTime = System.currentTimeMillis()
		logger.invoke(endTime - startTime, result)
		return result
	}
}