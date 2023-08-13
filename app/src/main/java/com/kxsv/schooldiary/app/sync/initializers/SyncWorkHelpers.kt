package com.kxsv.schooldiary.app.sync.initializers

import androidx.work.Constraints
import androidx.work.NetworkType


// All sync work needs an internet connectionS
val SyncConstraints
	get() = Constraints.Builder()
		.setRequiredNetworkType(NetworkType.CONNECTED)
		.build()