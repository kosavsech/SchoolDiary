package com.kxsv.schooldiary.ui.screens.update_dialog

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.ui.util.DismissibleDialog
import com.kxsv.schooldiary.util.Utils.getActivity
import com.pouyaheydari.appupdater.core.pojo.DialogStates
import com.pouyaheydari.appupdater.core.utils.getApk
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator


private const val TAG = "ShouldUpdateDialog"

@Destination(
	style = DismissibleDialog::class,
)
@Composable
fun ShouldUpdateDialog(
	destinationsNavigator: DestinationsNavigator,
	linkToApk: String,
	version: String,
	releaseNotes: String,
	viewModel: UpdateViewModel = hiltViewModel(),
) {
	val uiState = viewModel.uiState.collectAsState().value
	val activity = LocalContext.current.getActivity()
	var dialogProperties by remember { mutableStateOf(DismissibleDialog.properties) }
	val onUpdateClick = remember {
		{ viewModel.downloadApk() }
	}
	val onDismissClick = remember<() -> Unit> {
		{ destinationsNavigator.popBackStack() }
	}
	LaunchedEffect(uiState.dialogState) {
		if (uiState.dialogState is DialogStates.DownloadApk) {
			if (activity != null) {
				getApk(url = linkToApk, activity = activity)
				dialogProperties = DismissibleDialog.properties
			} else {
				Log.e(TAG, "Provided activity is null. Skipping downloading the apk")
			}
		}
	}
	UpdateDialogContent(
		dialogProperties = dialogProperties,
		dialogState = uiState.dialogState,
		title = R.string.new_version_found,
		releaseNotes = releaseNotes,
		version = version,
		onUpdateClick = onUpdateClick,
		onDismissClick = onDismissClick
	)
}