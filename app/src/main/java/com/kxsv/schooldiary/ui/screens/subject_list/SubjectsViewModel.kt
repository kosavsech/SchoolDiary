package com.kxsv.schooldiary.ui.screens.subject_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.util.DataIdGenUtils
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.util.Async
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import com.kxsv.schooldiary.util.Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class SubjectsUiState(
	val subjects: List<SubjectEntity> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

private const val TAG = "SubjectsViewModel"

@HiltViewModel
class SubjectsViewModel @Inject constructor(
	private val subjectRepository: SubjectRepository,
) : ViewModel() {
	
	private val _subjectsAsync =
		subjectRepository.observeAll()
			.map { Async.Success(it) }
			.catch<Async<List<SubjectEntity>>> { emit(Async.Error(R.string.loading_subjects_error)) }
	
	private val _uiState = MutableStateFlow(SubjectsUiState())
	val uiState: StateFlow<SubjectsUiState> = combine(
		_uiState, _subjectsAsync
	) { state, subjects ->
		when (subjects) {
			Async.Loading -> {
				SubjectsUiState(isLoading = true)
			}
			
			is Async.Error -> {
				SubjectsUiState(
					userMessage = subjects.errorMessage
				)
			}
			
			is Async.Success -> {
				SubjectsUiState(
					subjects = subjects.data,
					userMessage = state.userMessage,
					isLoading = state.isLoading
				)
			}
		}
	}.stateIn(viewModelScope, WhileUiSubscribed, SubjectsUiState(isLoading = true))
	
	private var fetchJob: Job? = null
	
	init {
		refresh()
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_subject_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_subject_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_subject_message)
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	fun refresh() {
		_uiState.update { it.copy(isLoading = true) }
		fetchJob?.cancel()
		fetchJob = viewModelScope.launch {
			val subjectNames = Utils.measurePerformanceInMS(
				logger = { time, _ ->
					Log.i(
						TAG, "fetchSubjectNames: performance is" +
								" ${(time / 10f).roundToInt() / 100f} S"
					)
				}
			) {
				subjectRepository.fetchSubjectNames()
			}
			Utils.measurePerformanceInMS(
				logger = { time, _ -> Log.i(TAG, "updateDatabase: performance is $time MS") }
			) {
				val fetchedSubjects = subjectNames.map { SubjectEntity(fullName = it) }
				fetchedSubjects.forEach { subject ->
					val subjectId = DataIdGenUtils.generateSubjectId(subject.fullName)
					val existedSubject = Utils.measurePerformanceInMS(
						{ time, result ->
							Log.d(
								TAG, "getSubject(${subjectId}): $time ms\n found = $result"
							)
						}
					) {
						subjectRepository.getSubject(subjectId)
					}
					if (existedSubject == null) {
						val newSubject = subject.copy(subjectId = subjectId)
						Log.i(TAG, "updateDatabase: FOUND NEW SUBJECT:\n${newSubject.fullName}")
						subjectRepository.updateSubject(newSubject, null)
					}
				}
			}
			_uiState.update { it.copy(isLoading = false) }
		}
	}
}
