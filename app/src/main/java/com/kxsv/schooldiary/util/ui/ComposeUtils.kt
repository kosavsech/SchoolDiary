/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kxsv.schooldiary.util.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarMonth
import kotlinx.coroutines.flow.filterNotNull
import java.time.DayOfWeek
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Display an initial empty state or swipe to refresh content.
 *
 * @param loading (state) when true, display a loading spinner over [content]
 * @param empty (state) when true, display [emptyContent]
 * @param emptyContent (slot) the content to display for the empty state
 * @param onRefresh (event) event to request refresh
 * @param modifier the modifier to apply to this layout.
 * @param content (slot) the main content to show
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoadingContent(
	loading: Boolean,
	isContentScrollable: Boolean = false,
	empty: Boolean,
	emptyContent: @Composable () -> Unit,
	onRefresh: () -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit,
) {
	val scrollModifier = if (!isContentScrollable) {
		Modifier.verticalScroll(rememberScrollState())
	} else {
		Modifier
	}
	val pullRefreshState = rememberPullRefreshState(loading, onRefresh)
	if (empty && !loading) {
		emptyContent()
	} else {
		Box(
			modifier = modifier
				.pullRefresh(pullRefreshState)
				.fillMaxSize()
				.then(scrollModifier),
		) {
			content()
			PullRefreshIndicator(loading, pullRefreshState, Modifier.align(Alignment.TopCenter))
		}
	}
}

@Composable
inline fun <reified T : ViewModel> NavBackStackEntry.sharedViewModel(
	navController: NavHostController,
): T {
	val navGraphRoute = destination.parent?.route ?: return hiltViewModel()
	val parentEntry = remember(this) {
		navController.getBackStackEntry(navGraphRoute)
	}
	return hiltViewModel(parentEntry)
}

/**
 * Alternative way to find the first fully visible month in the layout.
 *
 * @see [rememberFirstVisibleMonthAfterScroll]
 * @see [rememberFirstMostVisibleMonth]
 */
@Composable
fun rememberFirstCompletelyVisibleMonth(state: CalendarState): CalendarMonth {
	val visibleMonth = remember(state) { mutableStateOf(state.firstVisibleMonth) }
	// Only take non-null values as null will be produced when the
	// list is mid-scroll as no index will be completely visible.
	LaunchedEffect(state) {
		snapshotFlow { state.layoutInfo.completelyVisibleMonths.firstOrNull() }
			.filterNotNull()
			.collect { month -> visibleMonth.value = month }
	}
	return visibleMonth.value
}

private val CalendarLayoutInfo.completelyVisibleMonths: List<CalendarMonth>
	get() {
		val visibleItemsInfo = this.visibleMonthsInfo.toMutableList()
		return if (visibleItemsInfo.isEmpty()) {
			emptyList()
		} else {
			val lastItem = visibleItemsInfo.last()
			val viewportSize = this.viewportEndOffset + this.viewportStartOffset
			if (lastItem.offset + lastItem.size > viewportSize) {
				visibleItemsInfo.removeLast()
			}
			val firstItem = visibleItemsInfo.firstOrNull()
			if (firstItem != null && firstItem.offset < this.viewportStartOffset) {
				visibleItemsInfo.removeFirst()
			}
			visibleItemsInfo.map { it.month }
		}
	}

fun YearMonth.displayText(short: Boolean = false): String {
	return "${this.month.displayText(short = short)} ${this.year}"
}

fun Month.displayText(short: Boolean = true): String {
	val style = if (short) TextStyle.SHORT else TextStyle.FULL
	return getDisplayName(style, Locale.getDefault())
}

fun DayOfWeek.displayText(uppercase: Boolean = false): String {
	return getDisplayName(TextStyle.SHORT, Locale.getDefault()).let { value ->
		if (uppercase) value.uppercase(Locale.getDefault()) else value
	}
}