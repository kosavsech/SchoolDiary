package com.kxsv.schooldiary.ui.screens.grade_list

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.grade.GradeWithSubject
import com.kxsv.schooldiary.data.mapper.toGradeWithSubject
import com.kxsv.schooldiary.data.remote.util.NetworkException
import com.kxsv.schooldiary.data.repository.EduPerformanceRepository
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import com.kxsv.schooldiary.ui.util.GradesSortType
import com.kxsv.schooldiary.ui.util.WhileUiSubscribed
import com.kxsv.schooldiary.util.Utils.measurePerformanceInMS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.math.roundToInt

private const val TAG = "GradeTableViewModel"

data class GradesUiState(
	val grades: List<GradeWithSubject> = emptyList(),
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val sortType: GradesSortType = GradesSortType.MARK_DATE,
)

@HiltViewModel
class GradesViewModel @Inject constructor(
	private val gradeRepository: GradeRepository,
	private val subjectRepository: SubjectRepository,
	private val eduPerformanceRepository: EduPerformanceRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _sortType = MutableStateFlow(GradesSortType.MARK_DATE)
	
	@OptIn(ExperimentalCoroutinesApi::class)
	private val _gradesAsyncSorted = _sortType
		.flatMapLatest { sortType ->
			when (sortType) {
				GradesSortType.MARK_DATE -> gradeRepository.observeAllWithSubjectOrderedByMarkDate()
				GradesSortType.FETCH_DATE -> gradeRepository.observeAllWithSubjectOrderedByFetchDate()
			}
		}
		.stateIn(viewModelScope, WhileUiSubscribed, emptyList())
	
	private val _uiState = MutableStateFlow(GradesUiState())
	val uiState = combine(_uiState, _gradesAsyncSorted, _sortType) { state, gradesAsync, sortType ->
		state.copy(
			grades = gradesAsync,
			sortType = sortType
		)
	}.stateIn(viewModelScope, WhileUiSubscribed, GradesUiState())
	
	private var gradesFetchJob: Job? = null
	
	init {
		fetchGrades()
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_grade_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_grade_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_grade_message)
		}
	}
	
	fun sortGrades(sortType: GradesSortType) {
		_sortType.update { sortType }
	}
	
	fun snackbarMessageShown() {
		_uiState.update { it.copy(userMessage = null) }
	}
	
	fun fetchGrades() {
		_uiState.update { it.copy(isLoading = true) }
		gradesFetchJob?.cancel()
		gradesFetchJob = viewModelScope.launch(ioDispatcher) {
			try {
				val fetchedGradesWithTeachers = measurePerformanceInMS(
					logger = { time, _ ->
						Log.i(
							TAG,
							"fetchRecentGradesWithTeachers: performance is" +
									" ${(time / 10f).roundToInt() / 100f} S"
						)
					}
				) {
					gradeRepository.fetchRecentGradesWithTeachers()
				}
				val newGradeEntities = measurePerformanceInMS(
					logger = { time, _ ->
						Log.i(
							TAG,
							"updateDatabase: performance is $time MS"
						)
					}
				) {
					val fetchedGradesLocalized = mutableListOf<GradeWithSubject>()
					fetchedGradesWithTeachers.first.forEach {
						try {
							fetchedGradesLocalized.add(it.toGradeWithSubject(subjectRepository))
						} catch (e: NoSuchElementException) {
							Log.e(
								TAG,
								"updateTeachersDatabase: SchoolDiary: Couldn't localize grade ${it.mark}" +
										" on date ${it.date} for ${it.subjectAncestorName}.", e
							)
							// todo
							/*showSnackbarMessage(R.string.successfully_saved_grade_message)
							Toast.makeText(
								this.coroutineContext,
								"SchoolDiary: Couldn't localize grade ${it.mark} on date ${it.date} for ${it.subjectAncestorName}.\n" + e.message,
								Toast.LENGTH_LONG
							).show()*/
						}
					}
					for (fetchedGradeEntity in fetchedGradesLocalized) {
						val gradeId = fetchedGradeEntity.grade.gradeId
						val isGradeExisted = measurePerformanceInMS(
							{ time, result ->
								Log.d(
									TAG,
									"gradeDataSource.getById($gradeId): $time ms\n found = $result"
								)
							}
						) {
							
							gradeRepository.getGrade(gradeId) != null
						}
						gradeRepository.update(fetchedGradeEntity.grade)
						if (!isGradeExisted) {
							Log.i(
								TAG,
								"updateDatabase: FOUND NEW GRADE:\n${fetchedGradeEntity.grade}"
							)
						}
					}
				}
			} catch (e: NetworkException) {
				Log.e(TAG, "fetchGrades: exception on login", e)
			} catch (e: IOException) {
				Log.e(TAG, "fetchGrades: exception on response parseTerm", e)
			} catch (e: TimeoutCancellationException) {
				Log.e(TAG, "fetchGrades: connection timed-out", e)
				// TODO: show message that couldn't connect to site
			} catch (e: Exception) {
				Log.e(TAG, "fetchGrades: exception", e)
			} finally {
				_uiState.update { it.copy(isLoading = false) }
			}
		}
	}
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
}
