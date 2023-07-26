@file:OptIn(ExperimentalMaterialApi::class)

package com.kxsv.ychart_mod.ui.barchart

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import com.kxsv.ychart_mod.axis.XAxis
import com.kxsv.ychart_mod.axis.YAxis
import com.kxsv.ychart_mod.chartcontainer.container.ScrollableCanvasContainer
import com.kxsv.ychart_mod.common.components.ItemDivider
import com.kxsv.ychart_mod.common.components.accessibility.AccessibilityBottomSheetDialog
import com.kxsv.ychart_mod.common.components.accessibility.GroupBarInfo
import com.kxsv.ychart_mod.common.extensions.RowClip
import com.kxsv.ychart_mod.common.extensions.collectIsTalkbackEnabledAsState
import com.kxsv.ychart_mod.common.extensions.getMaxElementInYAxis
import com.kxsv.ychart_mod.common.extensions.isStackedBarTapped
import com.kxsv.ychart_mod.common.model.Point
import com.kxsv.ychart_mod.common.utils.ChartConstants
import com.kxsv.ychart_mod.ui.barchart.models.BarData
import com.kxsv.ychart_mod.ui.barchart.models.GroupBarChartData
import kotlinx.coroutines.launch

/**
 *
 * [StackedBarChart] compose method for drawing stacked bar chart.
 * @param modifier: All modifier related properties
 * @param groupBarChartData : All data needed to stacked bar chart
 * @see [GroupBarChartData] Data class to save all params related to stacked bar chart
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StackedBarChart(
	modifier: Modifier,
	groupBarChartData: GroupBarChartData,
	isTalkBackEnabled: Boolean = LocalContext.current.collectIsTalkbackEnabledAsState().value,
) {
	val accessibilitySheetState =
		rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
	val scope = rememberCoroutineScope()
	if (accessibilitySheetState.isVisible && isTalkBackEnabled
		&& groupBarChartData.accessibilityConfig.shouldHandleBackWhenTalkBackPopUpShown
	) {
		BackHandler {
			scope.launch {
				accessibilitySheetState.hide()
			}
		}
	}
	Surface(
		modifier
			.fillMaxSize()
			.testTag("stacked_bar_chart")
	) {
		with(groupBarChartData.barPlotData) {
			var visibility by remember { mutableStateOf(false) }
			var identifiedPoint by remember { mutableStateOf(BarData(Point(0f, 0f))) }
			var xOffset by remember { mutableStateOf(0f) }
			var tapOffset by remember { mutableStateOf(Offset(0f, 0f)) }
			var isTapped by remember { mutableStateOf(false) }
			var columnWidth by remember { mutableStateOf(0f) }
			var horizontalGap by remember { mutableStateOf(0f) }
			var rowHeight by remember { mutableStateOf(0f) }
			val paddingRight = groupBarChartData.paddingEnd
			val valueList = mutableListOf<Float>()
			groupBarList.map { groupBar ->
				var yMax = 0f
				groupBar.barList.forEach {
					yMax += it.point.y
				}
				valueList.add(yMax)
			}
			val bgColor = MaterialTheme.colorScheme.surface
			
			val xMax = groupBarList.size
			val yMax = valueList.maxOrNull() ?: 0f
			val xAxisData =
				groupBarChartData.xAxisData.copy(axisStepSize = barStyle.barWidth + barStyle.paddingBetweenBars)
			val yAxisData =
				groupBarChartData.yAxisData.copy(axisBottomPadding = LocalDensity.current.run { rowHeight.toDp() })
			
			val maxElementInYAxis = getMaxElementInYAxis(yMax, yAxisData.steps)
			val paddingBetweenBars =
				LocalDensity.current.run { groupBarChartData.paddingBetweenStackedBars.toPx() }
			
			if (!groupBarChartData.showXAxis) {
				rowHeight =
					LocalDensity.current.run { ChartConstants.DEFAULT_YAXIS_BOTTOM_PADDING.dp.toPx() }
			}
			ScrollableCanvasContainer(
				modifier = modifier
					.semantics {
						contentDescription = groupBarChartData.accessibilityConfig.chartDescription
					}
					.clickable {
						if (isTalkBackEnabled) {
							scope.launch {
								accessibilitySheetState.show()
							}
						}
					},
				containerBackgroundColor = groupBarChartData.backgroundColor,
				calculateMaxDistance = { xZoom ->
					horizontalGap = groupBarChartData.horizontalExtraSpace.toPx()
					val xLeft = (xAxisData.startDrawPadding.toPx() * xZoom) + horizontalGap
					xOffset =
						(barStyle.barWidth.toPx() + barStyle.paddingBetweenBars.toPx()) * xZoom
					getMaxScrollDistance(
						columnWidth = columnWidth,
						xMax = xMax.toFloat(),
						xMin = 0f,
						xOffset = xOffset,
						xLeft = xLeft,
						paddingRight = paddingRight.toPx(),
						canvasWidth = size.width
					)
				},
				onDraw = { scrollOffset, xZoom ->
					val isHighlightFullBar =
						barStyle.selectionHighlightData?.isHighlightFullBar ?: false
					val yBottom = size.height - rowHeight
					
					val totalPaddingBtwBars =
						(groupBarList.first().barList.size - 1) * paddingBetweenBars
					val yOffset =
						(yBottom - yAxisData.axisTopPadding.toPx() - totalPaddingBtwBars) / maxElementInYAxis
					xOffset =
						(barStyle.barWidth.toPx() + barStyle.paddingBetweenBars.toPx()) * xZoom
					val xLeft = columnWidth
					val dragLocks = mutableMapOf<Int, Pair<BarData, Offset>>()
					
					// Draw bar lines
					groupBarList.forEachIndexed { index, groupBarData ->
						var insideOffset = 0f
						val xPointOffset =
							groupBarData.barList.first().point.x * xOffset + xLeft + (xAxisData.startDrawPadding.toPx() * xZoom) - barStyle.barWidth.toPx() / 2 - scrollOffset
						val fullBarDetails = getFullBarDetails(
							groupBarData.barList,
							totalPaddingBtwBars,
							yOffset,
							yBottom,
							xPointOffset
						)
						
						groupBarData.barList.forEachIndexed { subIndex, individualBar ->
							val drawOffset = getGroupBarDrawOffset(
								x = index,
								y = individualBar.point.y,
								xOffset = xOffset,
								xLeft = xLeft,
								scrollOffset = scrollOffset,
								yBottom = yBottom,
								yOffset = yOffset,
								yMin = 0f,
								xMin = 0f,
								startDrawPadding = xAxisData.startDrawPadding.toPx(),
								zoomScale = xZoom,
								barWidth = barStyle.barWidth.toPx()
							)
							
							val height = yBottom - drawOffset.y
							
							val individualOffset = Offset(drawOffset.x, drawOffset.y - insideOffset)
							
							// drawing each individual bars
							groupBarChartData.drawBar(
								this,
								groupBarChartData,
								barStyle,
								individualOffset,
								height,
								subIndex
							)
							
							insideOffset += height + paddingBetweenBars
							
							val middleOffset =
								Offset(
									drawOffset.x + barStyle.barWidth.toPx() / 2,
									individualOffset.y
								)
							
							if (isTapped && middleOffset.isStackedBarTapped(
									tapOffset = tapOffset,
									barWidth = barStyle.barWidth.toPx(),
									barHeight = if (isHighlightFullBar) yBottom else individualOffset.y + height,
									tapPadding = groupBarChartData.tapPadding.toPx()
								)
							) {
								if (isHighlightFullBar) {
									dragLocks[0] = fullBarDetails.first to fullBarDetails.second
								} else {
									dragLocks[0] = individualBar to individualOffset
								}
							}
							
							drawUnderScrollMask(columnWidth, paddingRight, bgColor)
							
							if (barStyle.selectionHighlightData != null) {
								// highlighting the selected bar and showing the data points
								identifiedPoint = highlightGroupBar(
									dragLocks = dragLocks,
									visibility = visibility,
									identifiedPoint = identifiedPoint,
									selectionHighlightData = barStyle.selectionHighlightData,
									isDragging = isTapped,
									columnWidth = columnWidth,
									yBottom = yBottom,
									paddingRight = paddingRight,
									yOffset = yOffset,
									barWidth = barStyle.barWidth,
									totalPaddingBtwBars,
									isHighlightFullBar
								)
							}
						}
					}
				},
				drawXAndYAxis = { scrollOffset, xZoom ->
					val points = mutableListOf<Point>()
					for (index in groupBarList.indices) {
						points.add(Point(index.toFloat(), 0f))
					}
					
					if (groupBarChartData.showXAxis) {
						XAxis(
							xAxisData = xAxisData,
							modifier = Modifier
								.align(Alignment.BottomStart)
								.fillMaxWidth()
								.wrapContentHeight()
								.clip(
									RowClip(
										columnWidth, paddingRight
									)
								)
								.onGloballyPositioned {
									rowHeight = it.size.height.toFloat()
								},
							xStart = columnWidth,
							scrollOffset = scrollOffset,
							zoomScale = xZoom,
							chartData = points,
							axisStart = columnWidth
						)
					}
					if (groupBarChartData.showYAxis) {
						YAxis(
							modifier = Modifier
								.align(Alignment.TopStart)
								.fillMaxHeight()
								.wrapContentWidth()
								.onGloballyPositioned {
									columnWidth = it.size.width.toFloat()
								},
							yAxisData = yAxisData,
						)
					}
				},
				onPointClicked = { offset: Offset, _: Float ->
					isTapped = true
					visibility = true
					tapOffset = offset
				},
				onScroll = {
					isTapped = false
					visibility = false
				}
			)
		}
		if (isTalkBackEnabled) {
			with(groupBarChartData) {
				AccessibilityBottomSheetDialog(
					modifier = Modifier.fillMaxSize(), backgroundColor = Color.White, content = {
						LazyColumn(modifier = Modifier.semantics {
							this.testTag = "AccessibilityBottomSheet List"
						}) {
							items(barPlotData.groupBarList.size) { index ->
								Column {
									GroupBarInfo(
										barPlotData.groupBarList[index],
										xAxisData.axisLabelDescription(
											xAxisData.labelData(index)
										),
										barPlotData.barColorPaletteList,
										accessibilityConfig.titleTextSize,
										accessibilityConfig.descriptionTextSize
									)
									ItemDivider(
										thickness = accessibilityConfig.dividerThickness,
										dividerColor = accessibilityConfig.dividerColor
									)
								}
							}
						}
					},
					popUpTopRightButtonTitle = accessibilityConfig.popUpTopRightButtonTitle,
					popUpTopRightButtonDescription = accessibilityConfig.popUpTopRightButtonDescription,
					sheetState = accessibilitySheetState
				)
			}
		}
	}
}

