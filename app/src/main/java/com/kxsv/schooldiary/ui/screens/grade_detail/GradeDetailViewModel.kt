package com.kxsv.schooldiary.ui.screens.grade_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.repository.GradeRepository
import com.kxsv.schooldiary.ui.main.navigation.AppDestinationsArgs
import com.kxsv.schooldiary.util.Mark.Companion.getStringValueFrom
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class GradeDetailUiState(
	val mark: String = "",
	val typeOfWork: String = "",
	val gradeDate: LocalDate? = null,
	val subject: SubjectEntity? = null,
	val isLoading: Boolean = false,
	val userMessage: Int? = null,
)

@HiltViewModel
class GradeDetailViewModel @Inject constructor(
	private val gradeRepository: GradeRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	
	private val gradeId: String = savedStateHandle[AppDestinationsArgs.GRADE_ID_ARG]!!
	
	private val _uiState = MutableStateFlow(GradeDetailUiState())
	val uiState: StateFlow<GradeDetailUiState> = _uiState.asStateFlow()
	
	init {
		if (gradeId != "") loadGrade(gradeId)
	}
	
	fun snackbarMessageShown() {
		_uiState.update {
			it.copy(userMessage = null)
		}
	}
	
	private fun loadGrade(gradeId: String) {
		_uiState.update { it.copy(isLoading = true) }
		
		viewModelScope.launch {
			gradeRepository.getGradeWithSubject(gradeId).let { gradeWithSubject ->
				if (gradeWithSubject != null) {
					_uiState.update {
						it.copy(
							mark = getStringValueFrom(gradeWithSubject.grade.mark),
							typeOfWork = gradeWithSubject.grade.typeOfWork,
							gradeDate = gradeWithSubject.grade.date,
							subject = gradeWithSubject.subject,
							isLoading = false
						)
					}
				} else {
					_uiState.update { it.copy(isLoading = false) }
				}
			}
		}
	}
}