package com.pouyaheydari.appupdater.core.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

internal fun installAPK(context: Context, apk: File) {
	if (context.packageManager.canRequestPackageInstalls()) {
		try {
			val intent = Intent(Intent.ACTION_VIEW, getFileUri(context, apk)).run {
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
			}
			context.startActivity(intent)
		} catch (e: ActivityNotFoundException) {
			Log.e(TAG, e.message.orEmpty())
		}
	} else {
		context.showRequest()
	}
}

private fun getFileUri(context: Context, apk: File): Uri = FileProvider.getUriForFile(
	context, "${context.packageName}.fileProvider.GenericFileProvider", apk,
)
