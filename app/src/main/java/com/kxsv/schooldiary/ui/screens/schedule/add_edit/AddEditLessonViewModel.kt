package com.kxsv.schooldiary.ui.screens.schedule.add_edit


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.lesson.LessonEntity
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.repository.LessonRepository
import com.kxsv.schooldiary.data.repository.SubjectRepository
import com.kxsv.schooldiary.ui.screens.navArgs
import com.kxsv.schooldiary.util.Utils.timestampToLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AddEditScheduleUiState(
	val availableSubjects: List<SubjectEntity> = emptyList(),
	val initialSubjectSelection: Int? = null,
	val pickedSubject: SubjectEntity? = null,
	val classDate: LocalDate? = null,
	val classIndex: String = "",
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val isClassSaved: Boolean = false,
)

@HiltViewModel
class AddEditLessonViewModel @Inject constructor(
	private val lessonRepository: LessonRepository,
	private val subjectRepository: SubjectRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val addEditLessonScreenNavArgs: AddEditLessonDestinationNavArgs =
		savedStateHandle.navArgs()
	
	private val datestamp = addEditLessonScreenNavArgs.datestamp
	val lessonId = addEditLessonScreenNavArgs.lessonId
	
	private val _uiState = MutableStateFlow(AddEditScheduleUiState())
	val uiState: StateFlow<AddEditScheduleUiState> = _uiState.asStateFlow()
	
	init {
		if (lessonId != null) {
			loadClass(lessonId)
		} else {
			updateDate(timestampToLocalDate(datestamp)!!)
		}
		
	}
	
	fun saveLesson() {
		if (uiState.value.pickedSubject == null
			|| uiState.value.classDate == null
			|| uiState.value.classIndex.isBlank()
		) {
			_uiState.update {
				it.copy(userMessage = R.string.fill_required_fields_message)
			}
			return
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
	
	fun updateDate(date: LocalDate) {
		_uiState.update {
			it.copy(
				classDate = date
			)
		}
	}
	
	fun updateIndex(index: String) {
		_uiState.update {
			it.copy(
				classIndex = index
			)
		}
	}
	
	fun saveSelectedSubject(newIndex: Int) {
		val newPickedSubject: SubjectEntity = uiState.value.availableSubjects[newIndex]
		_uiState.update {
			it.copy(
				pickedSubject = newPickedSubject,
				initialSubjectSelection = null
			)
		}
	}
	
	fun loadAvailableSubjects() {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch {
			subjectRepository.getSubjects().let { subjects ->
				val updatedSelectedSubjectIndex: Int =
					subjects.binarySearch(uiState.value.pickedSubject, compareBy { it?.subjectId })
				
				_uiState.update {
					it.copy(
						availableSubjects = subjects,
						initialSubjectSelection = updatedSelectedSubjectIndex,
						isLoading = false
					)
				}
			}
		}
	}
	
	private fun createNewClass() = viewModelScope.launch {
		lessonRepository.createLesson(
			lesson = LessonEntity(
				index = uiState.value.classIndex.toInt() - 1,
				subjectAncestorId = uiState.value.pickedSubject!!.subjectId,
			),
			date = uiState.value.classDate!!
		)
		_uiState.update { it.copy(isClassSaved = true) }
	}
	
	private fun updateClass() {
		if (lessonId == null) throw RuntimeException("updateClass() was called but class is new.")
		
		viewModelScope.launch {
			lessonRepository.updateLesson(
				lesson = LessonEntity(
					index = uiState.value.classIndex.toInt() - 1,
					subjectAncestorId = uiState.value.pickedSubject!!.subjectId,
					lessonId = lessonId
				),
				date = uiState.value.classDate!!
			)
			_uiState.update {
				it.copy(isClassSaved = true)
			}
		}
	}
	
	private fun loadClass(subjectId: Long) {
		_uiState.update {
			it.copy(isLoading = true)
		}
		
		viewModelScope.launch {
			lessonRepository.getLessonWithSubject(subjectId)?.let { scheduleWithSubject ->
				_uiState.update {
					it.copy(
						pickedSubject = scheduleWithSubject.subject,
						classIndex = (scheduleWithSubject.lesson.index + 1).toString(),
					)
				}
			}
			lessonRepository.getLessonWithStudyDay(subjectId).let { scheduleWithStudyDay ->
				if (scheduleWithStudyDay != null) {
					_uiState.update {
						it.copy(
							classDate = scheduleWithStudyDay.studyDay.date,
							isLoading = false
						)
					}
				} else {
					_uiState.update {
						it.copy(isLoading = false)
					}
				}
			}
		}
	}
}