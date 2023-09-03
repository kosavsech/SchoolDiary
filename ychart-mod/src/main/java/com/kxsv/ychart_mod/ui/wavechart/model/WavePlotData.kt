package com.kxsv.ychart_mod.ui.wavechart.model

import com.kxsv.ychart_mod.common.model.PlotData
import com.kxsv.ychart_mod.common.model.PlotType

/**
 * WavePlotData is a data class that holds line graph related data and styling components
 * @param plotType : Defines the type of plot/graph
 * @param lines : Data related to the list of waves to be drawn.
 */
data class WavePlotData(
	override val plotType: PlotType = PlotType.Wave,
	val lines: List<Wave>,
) : PlotData {
	companion object {
		fun default() =
			WavePlotData(lines = listOf())
	}
}
