package com.kxsv.schooldiary.ui.screens.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.lesson.LessonWithSubject
import com.kxsv.schooldiary.data.local.features.study_day.StudyDayEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.PatternStrokeRepository
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.data.repository.TimePatternRepository
import com.kxsv.schooldiary.data.repository.UpdateRepository
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.main.navigation.ADD_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.DELETE_RESULT_OK
import com.kxsv.schooldiary.ui.main.navigation.EDIT_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AttendanceUiState(
	val studyDay: StudyDayEntity? = null,
	val classes: Map<Int, LessonWithSubject> = emptyMap(),
	val fetchedClasses: Map<Int, LessonWithSubject>? = null,
	val currentTimings: List<PatternStrokeEntity> = emptyList(),
	val selectedCalendarDay: CalendarDay? = null, // unique for DayScheduleCopyScreen
	
	val userMessage: Int? = null,
	val userMessageArgs: Array<out Any>? = null,
	val isLoading: Boolean = false,
)

@HiltViewModel
class AttendanceViewModel @Inject constructor(
	private val lessonRepository: LessonRepository,
	private val subjectRepository: SubjectRepository,
	private val strokeRepository: PatternStrokeRepository,
	private val studyDayRepository: StudyDayRepository,
	private val patternRepository: TimePatternRepository,
	private val userPreferencesRepository: UserPreferencesRepository,
	private val updateRepository: UpdateRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val _uiState = MutableStateFlow(AttendanceUiState())
	val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()
	
	private fun showSnackbarMessage(message: Int) {
		_uiState.update { it.copy(userMessage = message) }
	}
	
	private fun setSnackbarArgs(vararg args: Any) {
		_uiState.update { it.copy(userMessageArgs = args) }
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null, userMessageArgs = null)
		}
	}
	
	fun showEditResultMessage(result: Int) {
		when (result) {
			EDIT_RESULT_OK -> showSnackbarMessage(R.string.successfully_saved_class_message)
			ADD_RESULT_OK -> showSnackbarMessage(R.string.successfully_added_class_message)
			DELETE_RESULT_OK -> showSnackbarMessage(R.string.successfully_deleted_class_message)
		}
	}
	
}