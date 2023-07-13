package com.kxsv.schooldiary.ui.screens.schedule.add_edit


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.AppDestinationsArgs
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.features.schedule.Schedule
import com.kxsv.schooldiary.data.features.subject.Subject
import com.kxsv.schooldiary.domain.ScheduleRepository
import com.kxsv.schooldiary.domain.SubjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class AddEditScheduleUiState(
	val availableSubjects: List<Subject> = emptyList(),
	val initialSubjectSelection: Int? = null,
	val pickedSubject: Subject? = null,
	val classDate: LocalDate? = null,
	val classIndex: String = "",
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
	val isClassSaved: Boolean = false,
)

@HiltViewModel
class AddEditScheduleViewModel @Inject constructor(
	private val scheduleRepository: ScheduleRepository,
	private val subjectRepository: SubjectRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val scheduleId: Long = savedStateHandle[AppDestinationsArgs.SCHEDULE_ID_ARG]!!
	private val dateStamp: Long = savedStateHandle[AppDestinationsArgs.DATESTAMP_ARG]!!
	
	private val _uiState = MutableStateFlow(AddEditScheduleUiState())
	val uiState: StateFlow<AddEditScheduleUiState> = _uiState.asStateFlow()
	
	init {
		if (scheduleId != 0L) {
			loadClass(scheduleId)
		} else {
			updateDate(fromTimestamp(dateStamp))
		}
		
	}
	
	private fun fromTimestamp(value: Long): LocalDate =
		Instant.ofEpochSecond(value).atZone(ZoneId.of("UTC")).toLocalDate()
	
	
	fun saveSchedule() {
		if (uiState.value.pickedSubject == null
			|| uiState.value.classDate == null
			|| uiState.value.classIndex.isBlank()
		) {
			_uiState.update {
				it.copy(userMessage = R.string.fill_required_fields_message)
			}
			return
		}
		
		if (scheduleId == 0L) {
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
		val newPickedSubject: Subject = uiState.value.availableSubjects[newIndex]
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
		scheduleRepository.createSchedule(
			schedule = Schedule(
				index = uiState.value.classIndex.toInt() - 1,
				subjectAncestorId = uiState.value.pickedSubject!!.subjectId,
			),
			date = uiState.value.classDate!!
		)
		_uiState.update { it.copy(isClassSaved = true) }
	}
	
	private fun updateClass() {
		if (scheduleId == 0L) throw RuntimeException("updateClass() was called but class is new.")
		
		viewModelScope.launch {
			scheduleRepository.updateSchedule(
				schedule = Schedule(
					index = uiState.value.classIndex.toInt() - 1,
					subjectAncestorId = uiState.value.pickedSubject!!.subjectId,
					scheduleId = scheduleId
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
			scheduleRepository.getScheduleWithSubject(subjectId)?.let { scheduleWithSubject ->
				_uiState.update {
					it.copy(
						pickedSubject = scheduleWithSubject.subject,
						classIndex = scheduleWithSubject.schedule.index.toString(),
					)
				}
			}
			scheduleRepository.getScheduleWithStudyDay(subjectId).let { scheduleWithStudyDay ->
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