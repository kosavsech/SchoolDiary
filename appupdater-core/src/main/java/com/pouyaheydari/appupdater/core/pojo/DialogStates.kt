package com.pouyaheydari.appupdater.core.pojo


/**
 * Represents UI states of the update dialog
 */
sealed interface DialogStates {
    object ShowUpdateInProgress : DialogStates
    object HideUpdateInProgress : DialogStates
    object DownloadApk : DialogStates
}
