package com.kxsv.schooldiary.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

const val PERMISSION_REQUEST_CODE = 2008

/**
 * Check if application has a permission or not
 */
internal fun Context?.isPermissionGranted(permission: String): Boolean = if (this != null) {
	ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
} else {
	false
}

/**
 * shows get permission page to user
 */
internal fun Activity?.getPermission(permission: Array<String>) {
	if (this != null) {
		ActivityCompat.requestPermissions(this, permission, PERMISSION_REQUEST_CODE)
	} else {
		throw NullPointerException("Provided activity is null")
	}
}