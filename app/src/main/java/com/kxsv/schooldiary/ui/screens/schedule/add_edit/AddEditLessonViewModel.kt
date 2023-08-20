package com.kxsv.schooldiary.ui.screens.schedule.add_edit


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.StudyDayRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.util.Utils
import com.kxsv.schooldiary.util.Utils.nonEmptyTrim
import com.kxsv.schooldiary.util.Utils.timestampToLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditScheduleUiState(
	val pickedSubject: SubjectEntity? = null,
	val date: LocalDate? = null,
	val indexInPattern: String = "",
	val cabinet: String = "",
	
	val isCabinetFromSubject: Boolean = false,
	val availableSubjects: List<SubjectEntity> = emptyList(),
	val initialSubjectSelection: Int? = null,
	
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val indexErrorMessage: Int? = null,
	val cabinetErrorMessage: Int? = null,
	val isClassSaved: Boolean = false,
)

@HiltViewModel
class AddEditLessonViewModel @Inject constructor(
	private val lessonRepository: LessonRepository,
	private val studyDayRepository: StudyDayRepository,
	private val subjectRepository: SubjectRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val addEditLessonScreenNavArgs =
		savedStateHandle.navArgs<AddEditLessonDestinationNavArgs>()
	private val datestamp = addEditLessonScreenNavArgs.datestamp
	val lessonId = addEditLessonScreenNavArgs.lessonId
	
	private val _uiState = MutableStateFlow(AddEditScheduleUiState())
	val uiState: StateFlow<AddEditScheduleUiState> = _uiState.asStateFlow()
	
	init {
		if (lessonId != null) {
			loadLesson(lessonId)
		} else {
			val preSetDate = timestampToLocalDate(datestamp)
			updateDate(preSetDate ?: Utils.currentDate)
		}
	}
	
	fun saveLesson() {
		if (uiState.value.pickedSubject == null || uiState.value.date == null || uiState.value.indexInPattern.isBlank()) {
			_uiState.update {
				it.copy(userMessage = R.string.fill_required_fields_message)
			}
			return
		}
		if (uiState.value.indexInPattern.toIntOrNull() == null) {
			_uiState.update {
				it.copy(indexErrorMessage = R.string.wrong_input_format_for_class_index)
			}
			return
		} else {
			val classIndex = uiState.value.indexInPattern.toInt()
			if (classIndex < 1 || classIndex > 9) {
				_uiState.update {
					it.copy(indexErrorMessage = R.string.wrong_input_value_for_class_index)
				}
				return
			}
		}
		if (lessonId == null) {
			createNewClass()
		} else {
			updateClass()
		}
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	fun clearIndexErrorMessage() {
		_uiState.update {
			it.copy(indexErrorMessage = null)
		}
	}
	
	fun updateDate(date: LocalDate) {
		_uiState.update {
			it.copy(date = date)
		}
	}
	
	fun updateIndex(index: String) {
		_uiState.update {
			it.copy(indexInPattern = index)
		}
	}
	
	fun updateCabinet(cabinet: String) {
		_uiState.update {
			it.copy(
				cabinet = cabinet,
				isCabinetFromSubject = cabinet.isBlank()
			)
		}
	}
	
	fun saveSelectedSubject(newIndex: Int) {
		val newPickedSubject = uiState.value.availableSubjects[newIndex]
		_uiState.update {
			it.copy(
				pickedSubject = newPickedSubject,
				initialSubjectSelection = newIndex
			)
		}
	}
	
	fun loadAvailableSubjects() {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch(ioDispatcher) {
			subjectRepository.getAll().let { subjects ->
				val updatedInitialSelectionIndex = subjects
					.binarySearch(uiState.value.pickedSubject, compareBy { it?.displayName })
				
				_uiState.update {
					it.copy(
						availableSubjects = subjects,
						initialSubjectSelection = updatedInitialSelectionIndex,
						isLoading = false
					)
				}
			}
		}
	}
	
	private fun createNewClass() {
		val subjectAncestorId = uiState.value.pickedSubject?.subjectId
			?: throw RuntimeException("updateClass() was called but subject is not set.")
		val date = uiState.value.date
			?: throw RuntimeException("updateClass() was called but date is not set.")
		val cabinet = if (uiState.value.isCabinetFromSubject) {
			null
		} else {
			uiState.value.cabinet.nonEmptyTrim()
		}
		
		viewModelScope.launch(ioDispatcher) {
			lessonRepository.createLesson(
				lesson = LessonEntity(
					index = uiState.value.indexInPattern.toInt() - 1,
					subjectAncestorId = subjectAncestorId,
					cabinet = cabinet
				),
				date = date
			)
			_uiState.update { it.copy(isClassSaved = true) }
		}
	}
	
	private fun updateClass() {
		if (lessonId == null) throw RuntimeException("updateClass() was called but class is new.")
		val subjectAncestorId = uiState.value.pickedSubject?.subjectId
			?: throw RuntimeException("updateClass() was called but subject is not set.")
		val date = uiState.value.date
			?: throw RuntimeException("updateClass() was called but date is not set.")
		val cabinet = if (uiState.value.isCabinetFromSubject) {
			null
		} else {
			uiState.value.cabinet.nonEmptyTrim()
		}
		viewModelScope.launch(ioDispatcher) {
			lessonRepository.updateLesson(
				lesson = LessonEntity(
					index = uiState.value.indexInPattern.toInt() - 1,
					subjectAncestorId = subjectAncestorId,
					cabinet = cabinet,
					lessonId = lessonId
				),
				date = date
			)
			_uiState.update { it.copy(isClassSaved = true) }
		}
	}
	
	private fun loadLesson(lessonId: Long) {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch(ioDispatcher) {
			lessonRepository.getLessonWithSubject(lessonId)?.let { lessonWithSubject ->
				val isCabinetFromSubject =
					(lessonWithSubject.lesson.cabinet == null) && (lessonWithSubject.subject.cabinet != null)
				val cabinet =
					(lessonWithSubject.lesson.cabinet ?: lessonWithSubject.subject.cabinet) ?: ""
				val date = lessonWithSubject.lesson.studyDayMasterId?.let { studyDayMasterId ->
					studyDayRepository.getById(studyDayMasterId)?.date
				}
				_uiState.update {
					it.copy(
						pickedSubject = lessonWithSubject.subject,
						indexInPattern = (lessonWithSubject.lesson.index + 1).toString(),
						cabinet = cabinet,
						isCabinetFromSubject = isCabinetFromSubject,
						date = date
					)
				}
			} ?: _uiState.update { it.copy(isLoading = false) }
		}
	}
}