package com.kxsv.schooldiary.ui.screens.login

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.IoDispatcher
import com.kxsv.schooldiary.ui.screens.NavGraph
import com.kxsv.schooldiary.ui.screens.NavGraphs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SplashViewModel"

class SplashViewModel @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository,
	@IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _startDestination = mutableStateOf(NavGraphs.login)
	val startDestination: State<NavGraph> = _startDestination
	
	init {
		viewModelScope.launch(ioDispatcher) {
			val suppressed = async { userPreferencesRepository.getInitLoginSuppression() }
			val login = async { userPreferencesRepository.getEduLogin() }
			val password = async { userPreferencesRepository.getEduPassword() }
			if (suppressed.await()) {
				Log.e(TAG, "suppressed: ")
				_startDestination.value = NavGraphs.schedule
				login.cancel()
				password.cancel()
				return@launch
			}
			
			val firstTime = (login.await().isNullOrBlank() or password.await().isNullOrBlank())
			if (!firstTime) {
				Log.e(TAG, "NOT FIRST time: ")
				_startDestination.value = NavGraphs.schedule
				return@launch
			} else {
				Log.e(TAG, "first time: ")
				return@launch
			}
			
		}
	}
	
}