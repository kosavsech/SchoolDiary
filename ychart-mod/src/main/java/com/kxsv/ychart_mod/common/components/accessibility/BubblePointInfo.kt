package com.kxsv.ychart_mod.common.components.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * Composable to display each Bubble point data for given bubble chart.
 * @param axisLabelDescription: Axis label description.
 * @param pointDescription: Details of each point on the line.
 * @param bubbleColor: Color of each bubble.
 * @param titleTextSize: TextUnit title font size
 * @param descriptionTextSize: TextUnit description font size
 */
@Composable
fun BubblePointInfo(
	axisLabelDescription: String,
	pointDescription: String,
	bubbleColor: Color,
	titleTextSize: TextUnit,
	descriptionTextSize: TextUnit,
) {
	// Merge elements below for accessibility purposes
	Row(modifier = Modifier
		.padding(start = 10.dp, end = 10.dp)
		.clickable { }
		.semantics(mergeDescendants = true) {}, verticalAlignment = Alignment.CenterVertically
	) {
		Text(axisLabelDescription, fontSize = titleTextSize)
		Spacer(modifier = Modifier.width(5.dp))
		Column(
			modifier = Modifier
				.weight(1f)
				.padding(5.dp)
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box(
					modifier = Modifier
						.padding(5.dp)
						.background(bubbleColor)
						.size(20.dp)
				)
				Text(pointDescription, fontSize = descriptionTextSize)
			}
		}
	}
}
