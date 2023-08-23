package com.kxsv.schooldiary.data.repository

import android.util.Log
import com.kxsv.schooldiary.BuildConfig
import com.kxsv.schooldiary.data.remote.WebService
import com.kxsv.schooldiary.data.util.AppVersionState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "UpdateRepositoryImpl"

@Singleton
class UpdateRepositoryImpl @Inject constructor(
	private val webService: WebService,
//	@ApplicationScope private val scope: CoroutineScope, // to subscribe
) : UpdateRepository {
	
	private val _isUpdateAvailable = MutableSharedFlow<AppVersionState>(replay = 1)
	override val isUpdateAvailable = _isUpdateAvailable.asSharedFlow()
	
	override suspend fun checkUpdate() {
		val latestVersion = webService.getLatestAppVersion()
		val result = if (latestVersion == null) {
			AppVersionState.NotFound
		} else {
			if (latestVersion.versionCode > BuildConfig.VERSION_CODE) {
				if (latestVersion.isCritical) {
					AppVersionState.MustUpdate(latestVersion)
				} else {
					AppVersionState.ShouldUpdate(latestVersion)
				}
			} else {
				AppVersionState.LatestVersion
			}
		}
		_isUpdateAvailable.tryEmit(result)
		Log.d(TAG, "checkUpdate() returned: $result")
		return
	}
	
	override suspend fun suppressUpdateUntilNextAppStart() {
		_isUpdateAvailable.tryEmit(AppVersionState.Suppressed)
	}
	
}