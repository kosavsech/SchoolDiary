package com.kxsv.schooldiary.data.remote.dtos

import java.net.URL

data class UpdateDto(
	val version: String,
	val versionCode: Int,
	val apk: URL,
	val releaseNotes: String,
	val isCritical: Boolean,
)
