package com.kxsv.schooldiary.ui.screens.subject_list.subject_detail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithTeachers
import com.kxsv.schooldiary.data.mapper.toEduPerformanceEntities
import com.kxsv.schooldiary.data.repository.EduPerformanceRepository
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.user_preferences.Period
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.ui.util.Async
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.getCurrentPeriodForUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class SubjectDetailUiState(
	val subjectWithTeachers: SubjectWithTeachers? = null,
	val grades: List<GradeEntity> = emptyList(),
	val eduPerformance: EduPerformanceEntity? = null,
	val period: EduPerformancePeriod = EduPerformancePeriod.FOURTH,
	val isCalculatingUntilLowerBound: Boolean = false,
	val lowerBoundMark: Double = 0.0,
	val subjectsDaysOfWeek: List<DayOfWeek>? = null,
	val classesOnDayOfWeek: Map<DayOfWeek, Int>? = null,
	val lessonsLeft: Int? = null,
	val targetMark: Double = 0.0,
	val roundRule: Double = 0.6,
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

private data class AsyncData(
	val subjectWithTeachers: SubjectWithTeachers? = null,
	val grades: List<GradeEntity> = emptyList(),
	val eduPerformance: EduPerformanceEntity? = null,
	val subjectsDaysOfWeek: List<DayOfWeek> = emptyList(),
	val classesOnDayOfWeek: Map<DayOfWeek, Int> = emptyMap(),
)

private const val TAG = "SubjectDetailViewModel"

@Suppress("DeferredResultUnused")
@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
	private val subjectRepository: SubjectRepository,
	private val studyDayRepository: StudyDayRepository,
	private val eduPerformanceRepository: EduPerformanceRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	gradeRepository: GradeRepository,
	savedStateHandle: SavedStateHandle,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	val navArgs: SubjectDetailScreenNavArgs = savedStateHandle.navArgs()
	val subjectId = navArgs.subjectId
	
	private val _period = MutableStateFlow(EduPerformancePeriod.FOURTH)
	private val _periodType = MutableStateFlow(PeriodType.SEMESTERS)
	val periodType = _periodType.asStateFlow()
	
	private val _subjectWithTeachersAsync = subjectRepository.observeSubjectWithTeachers(subjectId)
	
	private val _gradesAsync = gradeRepository.observeGradesBySubjectId(subjectId)
	
	private val _weekSample1 = studyDayRepository.observeWeekSampleNext(Utils.currentDate)
	private val _weekSample2 = studyDayRepository.observeWeekSampleBefore(Utils.currentDate)
	
	private val _weekSample = combine(_weekSample1, _weekSample2) { weekSample1, weekSample2 ->
		return@combine if (weekSample1.isNotEmpty()) {
			if (weekSample2.isNotEmpty()) {
				if (weekSample2.size <= weekSample1.size) weekSample1 else weekSample2
			} else {
				weekSample1
			}
		} else weekSample2
	}
	
	private val _subjectsDaysOfWeek =
		combine(_subjectWithTeachersAsync, _weekSample) { subjectWithTeachersAsync, weekSample ->
			subjectWithTeachersAsync?.subject?.getDayOfWeekForSubject(weekSample) ?: emptyList()
		}
	private val _classesOnDayOfWeek =
		combine(_subjectWithTeachersAsync, _weekSample) { subjectWithTeachersAsync, weekSample ->
			subjectWithTeachersAsync?.subject?.getDayOfWeekAndClassAmountForSubject(weekSample)
				?: emptyMap()
		}
	private val _termsPeriodRanges = MutableStateFlow(emptyList<ClosedRange<LocalDate>>())
	private val _daysUntilPeriodEnd = MutableStateFlow(0)
	
	private val _lessonsLeft = combine(
		_subjectWithTeachersAsync,
		_weekSample,
		_termsPeriodRanges,
		_daysUntilPeriodEnd
	) { subjectWithTeachersAsync, weekSample, termsPeriodRanges, daysUntilPeriodEnd ->
		subjectWithTeachersAsync?.subject?.calculateLessonsLeftInThisPeriod(
			daysUntilPeriodEnd = daysUntilPeriodEnd,
			weekSample = weekSample,
			termsPeriodRanges = termsPeriodRanges
		)
	}
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _targetMark = _subjectWithTeachersAsync
		.flatMapLatest {
			if (it?.subject?.targetMark != null) {
				flowOf(it.subject.targetMark)
			} else {
				userPreferencesRepository.observeTargetMark()
			}
		}
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _lowerBoundMark = _subjectWithTeachersAsync
		.flatMapLatest {
			if (it?.subject?.lowerBoundMark != null) {
				flowOf(it.subject.lowerBoundMark)
			} else {
				userPreferencesRepository.observeLowerBoundMark()
			}
		}
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _eduPerformanceAsync = _period
		.flatMapLatest { period ->
			eduPerformanceRepository.observeEduPerformanceBySubject(subjectId, period)
		}
	
	private val _asyncData = combine(
		_subjectWithTeachersAsync,
		_gradesAsync,
		_eduPerformanceAsync,
		_subjectsDaysOfWeek,
		_classesOnDayOfWeek,
	) { subjectWithTeachers, grades, eduPerformance, subjectsDaysOfWeek, classesOnDayOfWeek ->
		AsyncData(
			subjectWithTeachers,
			grades,
			eduPerformance,
			subjectsDaysOfWeek,
			classesOnDayOfWeek
		)
	}
		.map { handleAsyncData(it) }
		.catch { emit(Async.Error(R.string.loading_subject_details_error)) }
		.stateIn(viewModelScope, WhileUiSubscribed, Async.Loading)
	
	private val _uiState = MutableStateFlow(SubjectDetailUiState())
	val uiState: StateFlow<SubjectDetailUiState> = Utils.combine(
		_uiState, _asyncData, _period, _targetMark, _lowerBoundMark, _lessonsLeft
	) { state, asyncData, period, targetMark, lowerBoundMark, lessonsLeft ->
		when (asyncData) {
			Async.Loading -> state.copy(isLoading = true)
			is Async.Error -> state.copy(userMessage = asyncData.errorMessage)
			is Async.Success -> state.copy(
				subjectWithTeachers = asyncData.data.subjectWithTeachers,
				grades = asyncData.data.grades,
				eduPerformance = asyncData.data.eduPerformance,
				subjectsDaysOfWeek = asyncData.data.subjectsDaysOfWeek,
				classesOnDayOfWeek = asyncData.data.classesOnDayOfWeek,
				lessonsLeft = lessonsLeft,
				period = period,
				targetMark = targetMark,
				lowerBoundMark = lowerBoundMark
			)
			
		}
		
	}.stateIn(viewModelScope, WhileUiSubscribed, SubjectDetailUiState(isLoading = true))
	
	init {
		viewModelScope.launch(ioDispatcher) {
			async {
				val periodType = userPreferencesRepository.getEducationPeriodType()
				_periodType.update { periodType }
				
				val allPeriodRanges = userPreferencesRepository.getPeriodsRanges()
				val daysUntilPeriodEnd = Utils.calculateDaysUntilPeriodEnd(
					allPeriodRanges = allPeriodRanges,
					periodType = periodType
				)
				_daysUntilPeriodEnd.update { daysUntilPeriodEnd ?: 0 }
				
				_termsPeriodRanges.update {
					allPeriodRanges
						.filter { Period.getTypeByPeriod(it.period) == PeriodType.TERMS }
						.map {
							val start = Utils.periodRangeEntryToLocalDate(it.range.start)
							val end = Utils.periodRangeEntryToLocalDate(it.range.end)
							return@map start..end
						}
				}
				val currentPeriod = getCurrentPeriodForUi(
					allPeriodRanges = allPeriodRanges,
					periodType = periodType
				)
				if (currentPeriod != null) {
					changePeriod(currentPeriod)
				}
			}
			async {
				_uiState.update {
					it.copy(roundRule = userPreferencesRepository.getRoundRule())
				}
			}
		}
	}
	
	fun deleteSubject() = viewModelScope.launch(ioDispatcher) {
		subjectRepository.deleteSubject(subjectId)
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update {
			it.copy(userMessage = message)
		}
	}
	
	private fun handleAsyncData(asyncData: AsyncData): Async<AsyncData> {
		if (asyncData.subjectWithTeachers == null) return Async.Error(R.string.subject_not_found)
//		if (asyncData.eduPerformance == null) return Async.Error(R.string.edu_performance_not_found)
		return Async.Success(asyncData)
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_subject_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_subject_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_subject_message)
		}
	}
	
	/**
	 * Refresh
	 *
	 * @throws NetworkException.NotLoggedInException
	 */
	fun refresh() {
		_uiState.update { it.copy(isLoading = true) }
		
		viewModelScope.launch(ioDispatcher) {
			try {
				eduPerformanceRepository.fetchEduPerformance().let {
					it.forEach { performanceDtos ->
						Log.d(
							TAG, "performanceEntities.forEach: performanceDtos = $performanceDtos"
						)
						eduPerformanceRepository.upsertAll(
							performanceDtos.toEduPerformanceEntities(
								subjectRepository
							)
						)
					}
				}
				_uiState.update { it.copy(isLoading = false) }
			} catch (e: Exception) {
				_uiState.update { it.copy(isLoading = false) }
				Log.e(TAG, "refresh: ", e)
				showSnackbarMessage(R.string.exception_occurred)
			}
		}
	}
	
	fun changePeriod(newPeriod: EduPerformancePeriod) {
		_period.update { newPeriod }
	}
	
	fun saveTargetMark(newTargetMark: Double) = viewModelScope.launch(ioDispatcher) {
		uiState.value.subjectWithTeachers?.let {
			subjectRepository.updateSubject(
				subject = it.subject.copy(targetMark = newTargetMark),
				teachersIds = null
			)
		}
	}
	
	fun saveLowerBoundMark(newLowerBoundMark: Double) = viewModelScope.launch(ioDispatcher) {
		uiState.value.subjectWithTeachers?.let {
			subjectRepository.updateSubject(
				subject = it.subject.copy(lowerBoundMark = newLowerBoundMark),
				teachersIds = null
			)
		}
	}
	
	fun changeBadMarkCalcType(newCalcType: Boolean) {
		_uiState.update {
			it.copy(isCalculatingUntilLowerBound = newCalcType)
		}
	}
	
}
