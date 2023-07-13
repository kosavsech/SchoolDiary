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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.util.ui.ContinuousSelectionHelper.isInDateBetweenSelection
import com.kxsv.schooldiary.util.ui.ContinuousSelectionHelper.isOutDateBetweenSelection
import kotlinx.coroutines.flow.filterNotNull
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

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
	emptyContent: @Composable () -> Unit = { Text(text = "Empty") },
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

/**
 * Modern Airbnb highlight style, as seen in the app.
 */
fun Modifier.backgroundHighlight(
	day: CalendarDay,
	today: LocalDate,
	selection: DateSelection,
	selectionColor: Color,
	continuousSelectionColor: Color,
	textColor: (Color) -> Unit,
): Modifier = composed {
	val (startDate, endDate) = selection
	val padding = 4.dp
	when (day.position) {
		DayPosition.MonthDate -> {
			when {
				startDate == day.date && endDate == null -> {
					textColor(Color.White)
					padding(padding)
						.background(color = selectionColor, shape = CircleShape)
				}
				
				day.date == startDate -> {
					textColor(Color.White)
					padding(vertical = padding)
						.background(
							color = continuousSelectionColor,
							shape = HalfSizeShape(clipStart = true),
						)
						.padding(horizontal = padding)
						.background(color = selectionColor, shape = CircleShape)
				}
				
				day.date == endDate -> {
					textColor(Color.White)
					padding(vertical = padding)
						.background(
							color = continuousSelectionColor,
							shape = HalfSizeShape(clipStart = false),
						)
						.padding(horizontal = padding)
						.background(color = selectionColor, shape = CircleShape)
				}
				
				startDate != null && endDate != null && (day.date > startDate && day.date < endDate) -> {
					val backgroundModifier = if (day.date.dayOfWeek != DayOfWeek.SUNDAY) {
						textColor(colorResource(R.color.example_4_grey))
						Modifier.background(
							color = continuousSelectionColor,
						)
					} else {
						textColor(Color.Red)
						Modifier.background(
							color = continuousSelectionColor.copy(alpha = 0.5f),
							shape = DottedShape(8.dp)
						)
					}
					padding(vertical = padding)
						.then(backgroundModifier)
				}
				
				day.date == today -> {
					textColor(colorResource(R.color.example_4_grey))
					padding(padding)
						.border(
							width = 1.dp,
							shape = CircleShape,
							color = colorResource(R.color.inactive_text_color),
						)
				}
				
				else -> {
					textColor(colorResource(R.color.example_4_grey))
					this
				}
			}
		}
		
		DayPosition.InDate -> {
			textColor(Color.Transparent)
			if (startDate != null && endDate != null &&
				isInDateBetweenSelection(day.date, startDate, endDate)
			) {
				padding(vertical = padding)
					.background(color = continuousSelectionColor)
			} else this
		}
		
		DayPosition.OutDate -> {
			textColor(Color.Transparent)
			if (startDate != null && endDate != null &&
				isOutDateBetweenSelection(day.date, startDate, endDate)
			) {
				padding(vertical = padding)
					.background(color = continuousSelectionColor)
			} else this
		}
	}
}

private class HalfSizeShape(private val clipStart: Boolean) : Shape {
	override fun createOutline(
		size: Size,
		layoutDirection: LayoutDirection,
		density: Density,
	): Outline {
		val half = size.width / 2f
		val offset = if (layoutDirection == LayoutDirection.Ltr) {
			if (clipStart) Offset(half, 0f) else Offset.Zero
		} else {
			if (clipStart) Offset.Zero else Offset(half, 0f)
		}
		return Outline.Rectangle(Rect(offset, Size(half, size.height)))
	}
}

private data class DottedShape(
	val step: Dp,
) : Shape {
	override fun createOutline(
		size: Size,
		layoutDirection: LayoutDirection,
		density: Density,
	) = Outline.Generic(Path().apply {
		val stepPx = with(density) { step.toPx() }
		val stepsCount = (size.width / stepPx).roundToInt()
		val actualStep = size.width / stepsCount
		val dotSize = Size(width = actualStep / 2, height = size.height)
		for (i in 0 until stepsCount) {
			addRect(
				Rect(
					offset = Offset(x = i * actualStep, y = 0f),
					size = dotSize
				)
			)
		}
		close()
	})
}