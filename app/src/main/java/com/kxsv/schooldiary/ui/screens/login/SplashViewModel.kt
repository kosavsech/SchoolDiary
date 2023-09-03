package com.kxsv.schooldiary.ui.screens.login

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kxsv.schooldiary.data.repository.UserPreferencesRepository
import com.kxsv.schooldiary.di.util.AppDispatchers
import com.kxsv.schooldiary.di.util.Dispatcher
import com.kxsv.schooldiary.ui.screens.NavGraphs
import com.ramcosta.composedestinations.spec.Route
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SplashViewModel"

class SplashViewModel @Inject constructor(
	private val userPreferencesRepository: UserPreferencesRepository,
	@Dispatcher(AppDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
	
	private val _startDestination: MutableState<Route> = mutableStateOf(NavGraphs.login)
	val startDestination: State<Route> = _startDestination
	
	init {
		viewModelScope.launch(ioDispatcher) {
			val startScreen = async { userPreferencesRepository.getStartScreen() }
			val suppressed = async { userPreferencesRepository.getInitLoginSuppression() }
			val login = async { userPreferencesRepository.getEduLogin() }
			val password = async { userPreferencesRepository.getEduPassword() }
			if (suppressed.await()) {
				Log.w(TAG, "suppressed: proceed to schedule screen")
				_startDestination.value = startScreen.await().value
				login.cancel()
				password.cancel()
				return@launch
			}
			
			val firstTime = (login.await().isNullOrBlank() or password.await().isNullOrBlank())
			if (!firstTime) {
				Log.i(TAG, "NOT FIRST time: ")
				_startDestination.value = startScreen.await().value
				return@launch
			} else {
				Log.i(TAG, "first time: ")
				return@launch
			}
			
		}
	}
	
}