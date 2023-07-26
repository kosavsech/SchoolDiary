package com.kxsv.ychart_mod.common.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.TextStyle
import com.kxsv.ychart_mod.axis.DataCategoryOptions
import com.kxsv.ychart_mod.common.model.LegendLabel
import com.kxsv.ychart_mod.common.model.LegendsConfig
import com.kxsv.ychart_mod.common.model.PlotType
import com.kxsv.ychart_mod.common.model.Point
import com.kxsv.ychart_mod.ui.barchart.models.BarChartType
import com.kxsv.ychart_mod.ui.barchart.models.BarData
import com.kxsv.ychart_mod.ui.barchart.models.GroupBar
import com.kxsv.ychart_mod.ui.bubblechart.model.Bubble
import com.kxsv.ychart_mod.ui.bubblechart.model.BubbleGradientType
import com.kxsv.ychart_mod.ui.bubblechart.model.BubbleStyle
import com.kxsv.ychart_mod.ui.linechart.model.SelectionHighlightPoint
import com.kxsv.ychart_mod.ui.linechart.model.SelectionHighlightPopUp
import com.kxsv.ychart_mod.ui.piechart.models.PieChartData
import kotlin.math.sin
import kotlin.random.Random

object DataUtils {
	/**
	 * Returns list of points
	 * @param listSize: Size of total number of points needed.
	 * @param start: X values to start from. ex: 50 to 100
	 * @param maxRange: Max range of Y values
	 */
	fun getLineChartData(listSize: Int, start: Int = 0, maxRange: Int): List<Point> {
		val list = arrayListOf<Point>()
		for (index in 0 until listSize) {
			list.add(
				Point(
					index.toFloat(),
					(start until maxRange).random().toFloat()
				)
			)
		}
		return list
	}
	
	/**
	 * Returns list of points
	 * @param listSize: Size of total number of points needed.
	 * @param start: X values to start from. ex: 50 to 100
	 * @param maxRange: Max range of Y values
	 */
	fun getRandomPoints(listSize: Int, start: Int = 0, maxRange: Int): List<Point> {
		val list = arrayListOf<Point>()
		for (index in 0 until listSize) {
			list.add(
				Point(
					index.toFloat(),
					(start until maxRange).random().toFloat()
				)
			)
		}
		return list
	}
	
	/**
	 * Returns list of points
	 * @param listSize: Size of total number of points needed.
	 * @param start: X values to start from. ex: 50 to 100
	 * @param maxRange: Max range of Y values
	 */
	fun getBubbleChartDataWithGradientStyle(
		points: List<Point>,
		minDensity: Float = 10F,
		maxDensity: Float = 100F,
	): List<Bubble> {
		val list = arrayListOf<Bubble>()
		points.forEachIndexed { index, point ->
			val bubbleColor1 = Color(
				Random.nextInt(256),
				Random.nextInt(256),
				Random.nextInt(256),
				Random.nextInt(256)
			)
			val bubbleColor2 = Color(
				Random.nextInt(256),
				Random.nextInt(256),
				Random.nextInt(256),
				Random.nextInt(256)
			)
			val bubbleColor3 = Color(
				Random.nextInt(256),
				Random.nextInt(256),
				Random.nextInt(256),
				Random.nextInt(256)
			)
			val bubbleColor4 = Color(
				Random.nextInt(256),
				Random.nextInt(256),
				Random.nextInt(256),
				Random.nextInt(256)
			)
			
			when (Random.nextInt(0, 5)) {
				0 -> {
					list.add(
						Bubble(
							center = point,
							density = (minDensity.toInt() until maxDensity.toInt()).random()
								.toFloat(),
							bubbleStyle = BubbleStyle(
								gradientColors = listOf(
									bubbleColor1,
									bubbleColor2,
									bubbleColor3,
									bubbleColor4
								),
								useGradience = true,
								gradientType = BubbleGradientType.RadialGradient
							),
							selectionHighlightPoint = SelectionHighlightPoint(Color.Black),
							selectionHighlightPopUp = SelectionHighlightPopUp(Color.White)
						)
					)
				}
				
				1 -> {
					list.add(
						Bubble(
							center = point,
							density = (minDensity.toInt() until maxDensity.toInt()).random()
								.toFloat(),
							bubbleStyle = BubbleStyle(
								gradientColors = listOf(
									bubbleColor1,
									bubbleColor2
								),
								useGradience = true,
								gradientType = BubbleGradientType.LinearGradient
							),
							selectionHighlightPoint = SelectionHighlightPoint(Color.Black),
							selectionHighlightPopUp = SelectionHighlightPopUp(Color.White)
						)
					)
				}
				
				2 -> {
					list.add(
						Bubble(
							center = point,
							density = (minDensity.toInt() until maxDensity.toInt()).random()
								.toFloat(),
							bubbleStyle = BubbleStyle(
								gradientColors = listOf(
									bubbleColor1,
									bubbleColor2
								),
								useGradience = true,
								gradientType = BubbleGradientType.VerticalGradient
							),
							selectionHighlightPoint = SelectionHighlightPoint(Color.Black),
							selectionHighlightPopUp = SelectionHighlightPopUp(Color.White)
						)
					)
				}
				
				3 -> {
					list.add(
						Bubble(
							center = point,
							density = (minDensity.toInt() until maxDensity.toInt()).random()
								.toFloat(),
							bubbleStyle = BubbleStyle(
								gradientColors = listOf(
									bubbleColor1,
									bubbleColor2
								),
								useGradience = true,
								gradientType = BubbleGradientType.HorizontalGradient
							),
							selectionHighlightPoint = SelectionHighlightPoint(Color.Black),
							selectionHighlightPopUp = SelectionHighlightPopUp(Color.White)
						)
					)
				}
				
				4 -> {
					list.add(
						Bubble(
							center = point,
							density = (minDensity.toInt() until maxDensity.toInt()).random()
								.toFloat(),
							bubbleStyle = BubbleStyle(
								gradientColors = listOf(
									bubbleColor1,
									bubbleColor2,
									bubbleColor3,
									bubbleColor4
								),
								useGradience = true,
								gradientType = BubbleGradientType.HorizontalGradient,
								tileMode = TileMode.Repeated
							),
							selectionHighlightPoint = SelectionHighlightPoint(Color.Black),
							selectionHighlightPopUp = SelectionHighlightPopUp(Color.White)
						)
					)
				}
				
				5 -> {
					list.add(
						Bubble(
							center = point,
							density = (minDensity.toInt() until maxDensity.toInt()).random()
								.toFloat(),
							bubbleStyle = BubbleStyle(
								gradientColors = listOf(
									bubbleColor1,
									bubbleColor2,
									bubbleColor3,
									bubbleColor4
								),
								useGradience = true,
								gradientType = BubbleGradientType.HorizontalGradient,
								tileMode = TileMode.Mirror
							),
							selectionHighlightPoint = SelectionHighlightPoint(Color.Black),
							selectionHighlightPopUp = SelectionHighlightPopUp(Color.White)
						)
					)
				}
			}
			
			
		}
		return list
	}
	
	/**
	 * Returns list of points
	 * @param listSize: Size of total number of points needed.
	 * @param start: X values to start from. ex: 50 to 100
	 * @param maxRange: Max range of Y values
	 */
	fun getBubbleChartDataWithSolidStyle(
		points: List<Point>,
		minDensity: Float = 10F,
		maxDensity: Float = 100F,
	): List<Bubble> {
		val list = arrayListOf<Bubble>()
		points.forEachIndexed { index, point ->
			val bubbleColor = Color(
				red = Random.nextInt(256),
				green = Random.nextInt(256),
				blue = Random.nextInt(256),
				alpha = Random.nextInt(from = 150, until = 256)
			)
			list.add(
				Bubble(
					center = point,
					density = (minDensity.toInt() until maxDensity.toInt()).random().toFloat(),
					bubbleStyle = BubbleStyle(solidColor = bubbleColor, useGradience = false),
					selectionHighlightPoint = SelectionHighlightPoint(Color.Black),
					selectionHighlightPopUp = SelectionHighlightPopUp(Color.White)
				)
			)
		}
		return list
	}
	
	/**
	 * Return the sample bar chart data
	 * @param duration : Duration of the wave in seconds
	 * @param sampleRate : Number of samples per second
	 * @param frequency : Frequency of the wave in Hz
	 */
	fun getWaveChartData(duration: Double, sampleRate: Double, frequency: Double): List<Point> {
		val list = mutableListOf<Point>()
		
		val amplitude = 1.0 // Amplitude of the wave
		val numSamples = (duration * sampleRate).toInt() // Total number of samples
		
		for (i in 0 until numSamples) {
			val time = i.toDouble() / sampleRate // Time at the current sample
			val sample =
				amplitude * sin(2.0 * Math.PI * frequency * time) // Calculate the sample value
			list.add(Point(time.toFloat(), sample.toFloat()))
		}
		return list
	}
	
	fun getBarChartData(
		listSize: Int,
		maxRange: Int,
		barChartType: BarChartType,
		dataCategoryOptions: DataCategoryOptions,
	): List<BarData> {
		val list = arrayListOf<BarData>()
		for (index in 0 until listSize) {
			val point = when (barChartType) {
				BarChartType.VERTICAL -> {
					Point(
						index.toFloat(),
						"%.2f".format(Random.nextDouble(1.0, maxRange.toDouble())).toFloat()
					)
				}
				
				BarChartType.HORIZONTAL -> {
					Point(
						"%.2f".format(Random.nextDouble(1.0, maxRange.toDouble())).toFloat(),
						index.toFloat()
					)
				}
			}
			
			list.add(
				BarData(
					point = point,
					color = Color(
						Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)
					),
					dataCategoryOptions = dataCategoryOptions,
					label = "Bar$index",
				)
			)
		}
		return list
	}
	
	/**
	 * Return the sample gradient bar chart data
	 * @param listSize Size of the list
	 * @param maxRange Maximum range for the values
	 */
	fun getGradientBarChartData(listSize: Int, maxRange: Int): List<BarData> {
		val list = arrayListOf<BarData>()
		for (index in 0 until listSize) {
			list.add(
				BarData(
					point = Point(
						index.toFloat(),
						"%.2f".format(Random.nextDouble(1.0, maxRange.toDouble())).toFloat()
					),
					gradientColorList = listOf(
						Color(
							Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)
						),
						Color(
							Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)
						),
						Color(
							Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)
						),
						Color(
							Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)
						)
					),
					label = "Bar$index"
				)
			)
		}
		return list
	}
	
	/**
	 * Returns sample pie chart data
	 */
	fun getPieChartData(): PieChartData {
		return PieChartData(
			slices = listOf(
				PieChartData.Slice("SciFi", 15f, Color(0xFF333333)),
				PieChartData.Slice("Comedy", 15f, Color(0xFF666a86)),
				PieChartData.Slice("Drama", 10f, Color(0xFF95B8D1)),
				PieChartData.Slice("Romance", 10f, Color(0xFFE8DDB5)),
				PieChartData.Slice("Action", 20f, Color(0xFFEDAFB8)),
				PieChartData.Slice("Thriller", 100f, Color(0xFFF94892)),
				PieChartData.Slice("Western", 10f, Color(0xFFA675A1)),
				PieChartData.Slice("Fantasy", 10f, Color(0xFF8F3985)),
			),
			plotType = PlotType.Pie
		)
	}
	
	/**
	 * Returns sample pie chart data
	 */
	fun getPieChartData2(): PieChartData {
		return PieChartData(
			slices = listOf(
				PieChartData.Slice("5", 3f, Color(0xFF5200D5)),
				PieChartData.Slice("4", 3f, Color(0xFF2A7511)),
				PieChartData.Slice("3", 1f, Color(0xFFF68300)),
				PieChartData.Slice("2", 0f, Color(0xFFD10000)),
			),
			plotType = PlotType.Pie
		)
	}
	
	/**
	 * Returns sample donut chart data
	 */
	fun getDonutChartData(): PieChartData {
		return PieChartData(
			slices = listOf(
				PieChartData.Slice("HP", 15f, Color(0xFF5F0A87)),
				PieChartData.Slice("Dell", 30f, Color(0xFF20BF55)),
				PieChartData.Slice("Lenovo", 10f, Color(0xFFA40606)),
				PieChartData.Slice("Asus", 15f, Color(0xFFF53844)),
				PieChartData.Slice("Acer", 10f, Color(0xFFEC9F05)),
				PieChartData.Slice("Apple", 30f, Color(0xFF009FFD)),
			),
			plotType = PlotType.Donut
		)
	}
	
	/**
	 * Returns the sample gradient bar chart data.
	 * @param listSize Size of the list
	 * @param maxRange Maximum range for the values
	 * @param barSize size of bars in one group
	 */
	fun getGroupBarChartData(listSize: Int, maxRange: Int, barSize: Int): List<GroupBar> {
		val list = mutableListOf<GroupBar>()
		for (index in 0 until listSize) {
			val barList = mutableListOf<BarData>()
			for (i in 0 until barSize) {
				val barValue = "%.2f".format(Random.nextDouble(1.0, maxRange.toDouble())).toFloat()
				barList.add(
					BarData(
						Point(
							index.toFloat(),
							barValue
						),
						label = "B$i",
						description = "Bar at $index with label B$i has value ${
							String.format(
								"%.2f", barValue
							)
						}"
					)
				)
			}
			list.add(GroupBar(index.toString(), barList))
		}
		return list
	}
	
	/**
	 * Returns the sample stackLabelList data
	 * @param colorPaletteList color list for each legend
	 */
	fun getLegendsLabelData(colorPaletteList: List<Color>): List<LegendLabel> {
		val legendLabelList = mutableListOf<LegendLabel>()
		for (index in colorPaletteList.indices) {
			legendLabelList.add(
				LegendLabel(
					colorPaletteList[index],
					"B$index"
				)
			)
		}
		return legendLabelList
	}
	
	/**
	 * Returns the sample colors list for given size
	 * @param listSize:  Size of the colors list.
	 */
	fun getColorPaletteList(listSize: Int): List<Color> {
		val colorList = mutableListOf<Color>()
		
		for (index in 0 until listSize) {
			colorList.add(
				Color(
					(0 until 256).random(), (0 until 256).random(), (0 until 256).random()
				)
			)
		}
		return colorList
	}
	
	/**
	 * Returns the legends config for given pie chart data
	 * @param pieChartData:  Pie chart details.
	 * @param gridSize: Legends grid size.
	 */
	fun getLegendsConfigFromPieChartData(pieChartData: PieChartData, gridSize: Int): LegendsConfig {
		val legendsList = mutableListOf<LegendLabel>()
		pieChartData.slices.forEach { slice ->
			legendsList.add(LegendLabel(slice.color, slice.label))
		}
		return LegendsConfig(
			legendLabelList = legendsList,
			gridColumnCount = gridSize,
			legendsArrangement = Arrangement.Start,
			textStyle = TextStyle()
		)
	}
}
