package com.kxsv.schooldiary.ui.screens.grade_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.ADD_EDIT_RESULT_OK
import com.kxsv.schooldiary.DELETE_RESULT_OK
import com.kxsv.schooldiary.EDIT_RESULT_OK
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.grade.Grade
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.di.IoDispatcher
import com.kxsv.schooldiary.domain.GradeRepository
import com.kxsv.schooldiary.domain.NetworkDataSource
import com.kxsv.schooldiary.domain.SubjectRepository
import com.kxsv.schooldiary.util.NetworkException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.time.LocalDate
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
	private val networkDataSource: NetworkDataSource,
	private val subjectRepository: SubjectRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	
	private val _uiState = MutableStateFlow(GradesUiState())
	val uiState: StateFlow<GradesUiState> = _uiState.asStateFlow()
	
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
	
	fun loadLocalGrades() = viewModelScope.launch(ioDispatcher) {
		_uiState.update { it.copy(isLoading = true) }
		val grades = measurePerformanceInMS(logger = { time, _ ->
			Log.i(TAG, "loadLocalGrades: performance is $time ms")
		}) { gradeRepository.getGradesWithSubjects() }
		_uiState.update { it.copy(grades = grades, isLoading = false) }
	}
	
	fun fetchGrades() {
		_uiState.update { it.copy(isLoading = true) }
		viewModelScope.coroutineContext.job.cancelChildren()
		viewModelScope.launch(ioDispatcher) {
			try {
				measurePerformanceInMS(logger = { time, _ ->
					Log.i(TAG, "fetchGrades: loadGradesForDate() performance is $time ms")
				}) {
					withTimeout(10000L) {
						for (i in 0 until 70) {
							async {
								val fetchedGradesLocalised: List<Grade> =
									networkDataSource.loadGradesForDate(
										LocalDate.of(2023, 2, 15).minusDays(i.toLong())
									).map { it.toLocal(subjectRepository) }
								gradeRepository.upsertAll(fetchedGradesLocalised)
							}
						}
					}
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
//				_uiState.update { it.copy(isLoading = false) }
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
