package com.kxsv.schooldiary.ui.screens.subject_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.edu_performance.EduPerformanceEntity
import com.kxsv.schooldiary.data.local.features.grade.GradeEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectWithTeachers
import com.kxsv.schooldiary.data.repository.EduPerformanceRepository
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.util.ui.Async
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
import com.kxsv.schooldiary.util.ui.WhileUiSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubjectDetailUiState(
	val subjectWithTeachers: SubjectWithTeachers? = null,
	val grades: List<GradeEntity> = emptyList(),
	val eduPerformance: EduPerformanceEntity? = null,
	val period: EduPerformancePeriod = EduPerformancePeriod.FOURTH_TERM,
	val targetMark: Double = 0.0,
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val isSubjectDeleted: Boolean = false,
)

private data class AsyncData(
	val subjectWithTeachers: SubjectWithTeachers? = null,
	val grades: List<GradeEntity> = emptyList(),
	val eduPerformance: EduPerformanceEntity? = null,
)

@HiltViewModel
class SubjectDetailViewModel @Inject constructor(
	private val subjectRepository: SubjectRepository,
	private val eduPerformanceRepository: EduPerformanceRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	gradeRepository: GradeRepository,
	savedStateHandle: SavedStateHandle,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	val subjectId: Long = savedStateHandle[AppDestinationsArgs.SUBJECT_ID_ARG]!!
	
	private val _period = MutableStateFlow(EduPerformancePeriod.FOURTH_TERM)
	
	private val _subjectWithTeachersAsync = subjectRepository.observeSubjectWithTeachers(subjectId)
	
	private val _gradesAsync = gradeRepository.getGradesBySubjectIdStream(subjectId)
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _targetMark = _subjectWithTeachersAsync
		.flatMapLatest {
			if (it.subject.targetMark != null) {
				flowOf(it.subject.targetMark)
			} else {
				userPreferencesRepository.observeTargetMark()
			}
		}
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _eduPerformanceAsync = _period
		.flatMapLatest { period ->
			eduPerformanceRepository.observeEduPerformanceBySubject(subjectId, period)
		}
	
	private val asyncData = combine(
		_subjectWithTeachersAsync, _gradesAsync, _eduPerformanceAsync
	) { subjectWithTeachers, grades, eduPerformance ->
		AsyncData(subjectWithTeachers, grades, eduPerformance)
	}
		.map { handleAsyncData(it) }
		.catch { emit(Async.Error(R.string.loading_subject_details_error)) }
		.stateIn(viewModelScope, WhileUiSubscribed, Async.Loading)
	
	private val _uiState = MutableStateFlow(SubjectDetailUiState())
	val uiState: StateFlow<SubjectDetailUiState> = combine(
		_uiState, asyncData, _period, _targetMark
	) { state, asyncData, period, targetMark ->
		when (asyncData) {
			Async.Loading -> state.copy(isLoading = true)
			is Async.Error -> state.copy(userMessage = asyncData.errorMessage)
			is Async.Success -> state.copy(
				subjectWithTeachers = asyncData.data.subjectWithTeachers,
				grades = asyncData.data.grades,
				eduPerformance = asyncData.data.eduPerformance,
				period = period,
				targetMark = targetMark
			)
		}
		
	}.stateIn(viewModelScope, WhileUiSubscribed, SubjectDetailUiState(isLoading = true))
	
	fun deleteSubject() = viewModelScope.launch {
		subjectRepository.deleteSubject(subjectId)
		_uiState.update {
			it.copy(isSubjectDeleted = true)
		}
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
		if (asyncData.eduPerformance == null) return Async.Error(R.string.edu_performance_not_found)
		return Async.Success(asyncData)
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_grade_message)
			ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_grade_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_grade_message)
		}
	}
	
	fun changePeriod(newPeriod: EduPerformancePeriod) {
		_period.update { newPeriod }
	}
	
	fun changeTargetMark(newTargetMark: Double) = viewModelScope.launch(ioDispatcher) {
		uiState.value.subjectWithTeachers?.subject?.let {
			subjectRepository.updateSubject(
				subject = it.copy(
					targetMark = newTargetMark
				)
			)
		}
	}
}
