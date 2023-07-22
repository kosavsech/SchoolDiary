package com.kxsv.schooldiary.ui.screens.grade_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.util.remote.NetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

private const val TAG = "GradesViewModel"

data class GradesUiState(
	val grades: List<GradeWithSubject> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

@HiltViewModel
class GradesViewModel @Inject constructor(
	private val gradeRepository: GradeRepository,
	private val subjectRepository: SubjectRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _uiState = MutableStateFlow(GradesUiState())
	val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()
	
	private var gradesFetchJob: Job? = null
	
	init {
		loadLocalGrades()
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_grade_message)
			ADD_EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_grade_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_grade_message)
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	private fun loadLocalGrades() = viewModelScope.launch(ioDispatcher) {
		_uiState.update { it.copy(isLoading = true) }
		val grades = measurePerformanceInMS(logger = { time, _ ->
			Log.i(TAG, "loadLocalGrades: performance is $time ms")
		}) { gradeRepository.getGradesWithSubjects() }
		_uiState.update { it.copy(grades = grades, isLoading = false) }
	}
	
	fun fetchGrades() {
		_uiState.update { it.copy(isLoading = true) }
		gradesFetchJob?.cancel()
		gradesFetchJob = viewModelScope.launch(ioDispatcher) {
			try {
				measurePerformanceInMS({ time, _ ->
					Log.i(TAG, "fetchGrades: loadGradesForDate() performance is $time ms")
				}) {
					// fetch all grades
				}
			} catch (e: NetworkException) {
				Log.e(TAG, "fetchGrades: exception on login", e)
			} catch (e: IOException) {
				Log.e(TAG, "fetchGrades: exception on response parse", e)
			} catch (e: TimeoutCancellationException) {
				Log.e(TAG, "fetchGrades: connection timed-out", e)
				// TODO: show message that couldn't connect to site
			} catch (e: Exception) {
				Log.e(TAG, "fetchGrades: exception", e)
			} finally {
				loadLocalGrades()
			}
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
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
