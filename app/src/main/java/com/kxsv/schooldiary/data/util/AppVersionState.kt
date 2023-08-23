package com.kxsv.schooldiary.data.util

import com.kxsv.schooldiary.data.remote.dtos.UpdateDto

sealed interface AppVersionState {
	object Suppressed : AppVersionState
	object LatestVersion : AppVersionState
	object NotFound : AppVersionState
	data class ShouldUpdate(val update: UpdateDto) : AppVersionState
	data class MustUpdate(val update: UpdateDto) : AppVersionState
}