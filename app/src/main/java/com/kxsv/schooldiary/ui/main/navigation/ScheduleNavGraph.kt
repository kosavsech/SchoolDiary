package com.kxsv.schooldiary.ui.main.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph()
@NavGraph()
annotation class ScheduleNavGraph(
	val start: Boolean = false,
)