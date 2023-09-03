package com.kxsv.schooldiary.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.Mark
import com.kxsv.schooldiary.data.util.user_preferences.Period
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.data.util.user_preferences.PeriodWithRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

private const val TAG = "Utils"

object Utils {
	val taskDueDateFormatterLong: DateTimeFormatter = DateTimeFormatter.ofPattern("eeee, MMMM d")
	val currentDate: LocalDate = LocalDate.now()
	
	/**
	 * Tries to return an [Activity] instance from the compose [LocalContext]
	 *
	 * @return Either an [Activity] instance or null
	 */
	internal fun Context.getActivity(): Activity? = when (this) {
		is Activity -> this
		is ContextWrapper -> baseContext.getActivity()
		else -> null
	}
	
	
	fun Collection<PeriodWithRange>.getCurrentPeriod(): EduPerformancePeriod? {
		if (this.isEmpty()) return null
		
		val lastPeriodEnd =
			periodRangeEntryToLocalDate(this.maxBy { it.period.ordinal }.range.end)
		
		var holidayCandidate: Pair<PeriodWithRange?, Long> = Pair(null, 366)
		if (currentDate.isAfter(lastPeriodEnd)) {
			return EduPerformancePeriod.FOURTH
		} else {
			this.forEach { periodWithRange ->
				val startDate = periodRangeEntryToLocalDate(periodWithRange.range.start)
				val endDate = periodRangeEntryToLocalDate(periodWithRange.range.end)
				val periodTimeRange = startDate..endDate
				
				if (currentDate in periodTimeRange) {
					/*Log.d(
						TAG,
						"getCurrentPeriod() returned: ${periodWithRange.period.convertToEduPerformancePeriod()}"
					)*/
					return periodWithRange.period.convertToEduPerformancePeriod()
				} else {
					val daysUntilEnd = currentDate.until(endDate, ChronoUnit.DAYS)
					if (daysUntilEnd > 0) {
//						Log.d(TAG, "getCurrentPeriod() skipped: $periodWithRange")
						return@forEach
					}
					if (daysUntilEnd < holidayCandidate.second) {
//						Log.d(TAG, "getCurrentPeriod: was $holidayCandidate")
						holidayCandidate = Pair(periodWithRange, daysUntilEnd)
//						Log.d(TAG, "getCurrentPeriod: become $holidayCandidate")
					}
				}
			}
		}
		
		val result = if (holidayCandidate.first != null) {
			holidayCandidate.first!!.period.convertToEduPerformancePeriod()
		} else {
			null
		}
//		Log.d(TAG, "getCurrentPeriod() returned candidate: $result")
		return result
	}
	
	fun List<PatternStrokeEntity>.getCurrentLessonIndexByTime(currentTime: LocalTime): Int? {
		if (this.isEmpty()) return null
		val studyTimeRange = this.first().startTime..this.last().endTime
		if (currentTime !in studyTimeRange) return null
		
		this.forEach { timing ->
			val lessonTimeRange = timing.startTime..timing.endTime
			if (currentTime in lessonTimeRange) return timing.index
		}
		return null
	}
	
	/**
	 * Get index of closest next lesson to time.
	 *
	 * @param time
	 * @param pattern
	 * @return index of closest future lesson. Or null if couldn't find one.
	 */
	fun Collection<Int>.getIndexOfClosestLessonToTime(
		time: LocalTime,
		pattern: List<PatternStrokeEntity>,
	): Int? {
		if (pattern.isEmpty()) throw IllegalStateException("Shouldn't be called with empty pattern")
		if (this.isEmpty()) throw IllegalStateException("Shouldn't be called with empty classes indices")
		
		val studyTimeRange = pattern.first().startTime..pattern.last().endTime
		val result = if (time !in studyTimeRange && time.isBefore(studyTimeRange.start)) {
			this.min()
		} else {
			var candidateWithTimings: Pair<Int?, Long> = Pair(null, 1440)
			val candidatesWithoutTimings = mutableListOf<Int>()
			this.forEach {
				val currentStroke = pattern.getOrNull(it)
				if (currentStroke == null) {
					candidatesWithoutTimings.add(it)
					return@forEach
				}
				val lessonTime = currentStroke.startTime..currentStroke.endTime
				if (time !in lessonTime && time.isBefore(lessonTime.start)) {
					
					val timeUntilLesson = time.until(lessonTime.start, ChronoUnit.MINUTES)
					if (timeUntilLesson < candidateWithTimings.second) {
						candidateWithTimings = Pair(it, timeUntilLesson)
					}
				}
			}
			candidateWithTimings.first ?: candidatesWithoutTimings.firstOrNull()
		}
		
		return result
	}
	
	/**
	 * Gets next [n] lessons indices after [startIndex]
	 *
	 * @param n how much lessons to take
	 * @param startIndex starting index to search lessons
	 * @return
	 */
	fun Collection<Int>.getNextLessonsIndices(n: Int, startIndex: Int?): List<Int>? {
		if (startIndex == null) {
//			Log.d(TAG, "getNextLessonsIndices: startIndex == null")
			return null
		}
		if (this.isEmpty()) return null

//		Log.d(TAG, "getNextLessonsIndices: this: $this")
		val indicesSublist = this.filter { it >= startIndex }.toList()
//		Log.d(TAG, "getNextLessonsIndices: indicesSublist: $indicesSublist")
		if (indicesSublist.isEmpty()) {
//			Log.d(TAG, "getNextLessonsIndices: indicesSublist.isEmpty()")
			return null
		}
		
		val result = mutableListOf<Int>()
		indicesSublist.forEach {
			if (result.size == n) return result
			result.add(it)
		}
//		Log.d(TAG, "getNextLessonsIndices() returned: $result")
		return result
	}
	
	fun fromLocalTime(localTime: LocalTime?): String? =
		localTime?.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
	
	
	fun localDateToPeriodRangeEntry(date: LocalDate): String =
		"${date.monthValue}_${date.dayOfMonth}"
	
	fun periodRangeEntryToLocalDate(value: String): LocalDate = value.let {
		val periodMonth = it.split("_")[0].toInt()
		val dayOfMonth = it.split("_")[1].toInt()
		
		val firstPartOfStudyYear = 9..12
		val secondPartOfStudyYear = 1..5
		
		val currentMonth = currentDate.monthValue
		val currentYear = currentDate.year
		
		val year = when (currentMonth) {
			in firstPartOfStudyYear -> {
				when (periodMonth) {
					in secondPartOfStudyYear -> currentYear + 1
					else -> currentYear
				}
			}
			
			else -> {
				when (periodMonth) {
					in firstPartOfStudyYear -> currentYear - 1
					else -> currentYear
				}
			}
		}
		return LocalDate.of(year, periodMonth, dayOfMonth)
	}
	
	fun datestampToLocalDate(value: Long?): LocalDate? =
		value?.let { Instant.ofEpochSecond(it).atZone(ZoneId.of("Europe/Moscow")).toLocalDate() }
	
	fun localDateToDatestamp(date: LocalDate?): Long? =
		date?.atStartOfDay(ZoneId.of("Europe/Moscow"))?.toEpochSecond()
	
	fun Double.stringRoundTo(n: Int): String {
		return String.format("%.${n}f", this, Locale.ENGLISH)
	}
	
	fun Double.roundTo(n: Int): Double {
		return String.format("%.${n}f", this, Locale.ENGLISH).toDouble()
	}
	
	fun Float.stringRoundTo(n: Int): String {
		return String.format("%.${n}f", this, Locale.ENGLISH)
	}
	
	fun Float.roundTo(n: Int): Float {
		return String.format("%.${n}f", this, Locale.ENGLISH).toFloat()
	}
	
	fun roundWithRule(x: Double, roundRule: Double): Double {
		val floored = floor(x)
		return if (x >= (floored + roundRule)) {
			ceil(x)
		} else {
			floored
		}
	}
	
	/**
	 * @param x which is [grade][Mark.value]
	 * @throws IllegalArgumentException if param [x] > [5][Mark.FIVE] or [x] < [2][Mark.TWO]
	 */
	fun getLowerBoundOfGrade(x: Int, roundRule: Double): Double {
		if (x > Mark.FIVE.value!! || x < Mark.TWO.value!!) throw IllegalArgumentException("Non-existent grade")
		return x - 1 + roundRule
	}
	
	/**
	 * @param x is mark with type [Double] so could consume any number of marks.
	 * @throws IllegalArgumentException if param [x] > [5][Mark.FIVE] or [x] < [2][Mark.TWO]
	 */
	fun getLowerBoundForMark(x: Double, roundRule: Double): Double {
		if (x > Mark.FIVE.value!! || x < Mark.TWO.value!!) throw IllegalArgumentException("Non-existent mark")
		return roundWithRule(x, roundRule) - 1 + roundRule
	}
	
	/**
	 *
	 * @return null if string is either null or empty or consists solely
	 * of whitespace characters, else trimmed self
	 */
	fun String?.nonEmptyTrim(): String? {
		return this.let {
			if (it.isNullOrBlank()) {
				return@let null
			} else {
				return@let it.trim()
			}
		}
	}
	
	fun calculateMarksUntilTarget(
		target: Double,
		avgMark: Double,
		sum: Int,
		valueSum: Double,
	): Map<Int, Int?> {
		fun calculateGrades(grade: Int): Int? {
			var processAvg = avgMark
			var gradeCount: Int = if (processAvg >= grade) return null else 0
			while (processAvg < target && gradeCount < 81) {
				gradeCount++
				processAvg = (valueSum + grade * gradeCount) / (sum + gradeCount)
			}
			return gradeCount
		}
		
		val result = mutableMapOf<Int, Int?>()
		for (i in 3..5) {
			result[i] = calculateGrades(grade = i)
		}
		return result
	}
	
	private fun <T> List<T>.isEqualsIgnoreOrder(other: List<T>) =
		this.size == other.size && this.toSet() == other.toSet()
	
	fun calculateRealizableBadMarks(
		roundRule: Double,
		lowerBound: Double,
		avgMark: Double,
		sum: Int,
		valueSum: Double,
	): List<RealizableBadMarks> {
		fun calculateGrades(strategy: BadMarkStrategy): RealizableBadMarks? {
			var currentInProcessAvg = avgMark
			val strategyGrades = strategy.getIntValuesOfGrades()
			if (strategyGrades.first() == null) return null
			when (strategyGrades.size) {
				1 -> {
					var firstGradeCount: Int =
						if (strategyGrades.first()!!
							>= roundWithRule(currentInProcessAvg, roundRule)
						) {
							Log.w(TAG, "calculateGrades: auto-skip of ${strategyGrades[0]}")
							return null
						} else {
							1
						}
					
					while (true) {
						val valueOfFirstGradeAdded = strategyGrades.first()!! * firstGradeCount
						val finalValue = valueSum + valueOfFirstGradeAdded
						val finalSum = sum + firstGradeCount
						
						(finalValue / finalSum).let {
							if (it >= lowerBound) {
								Log.d(TAG, "currentInProcessAvg: $it > $lowerBound")
								currentInProcessAvg = it
							} else {
								return if ((firstGradeCount - 1) != 0) {
									val result = RealizableBadMarks(
										strategy = strategy,
										count = listOf(firstGradeCount - 1)
									)
									Log.d(TAG, "calculateGrades(1) returned: $result")
									result
								} else {
									null
								}
							}
						}
						firstGradeCount++
					}
					
				}
				
				2 -> {
					val maxGrade = maxOf(strategyGrades.first()!!, strategyGrades.last()!!)
					if (maxGrade >= roundWithRule(currentInProcessAvg, roundRule)) {
						Log.w(
							TAG,
							"calculateGrades: auto-skip of ${strategyGrades[0]} and ${strategyGrades[1]}"
						)
						return null
					}
					
					var applyingFirstGrade = true
					var firstGradeCount = 1
					var secondGradeCount = 0
					
					var isSecondGradeBanned = false
					var isFirstGradeBanned = false
					
					while (true) {
						val valueOfFirstGradeAdded = strategyGrades.first()!! * firstGradeCount
						val valueOfSecondGradeAdded = strategyGrades.last()!! * secondGradeCount
						val finalValue =
							valueSum + valueOfFirstGradeAdded + valueOfSecondGradeAdded
						val finalSum = sum + firstGradeCount + secondGradeCount
						
						(finalValue / finalSum).let {
							if (it >= lowerBound) {
								currentInProcessAvg = it
							} else {
								if (applyingFirstGrade) {
									firstGradeCount--
									isFirstGradeBanned = true
								} else {
									secondGradeCount--
									isSecondGradeBanned = true
								}
							}
						}
						
						applyingFirstGrade = if (isFirstGradeBanned || isSecondGradeBanned) {
							if (isFirstGradeBanned && isSecondGradeBanned) {
								return if (firstGradeCount != 0 && secondGradeCount != 0) {
									val result = RealizableBadMarks(
										strategy = strategy,
										count = listOf(firstGradeCount, secondGradeCount)
									)
									result
								} else {
									null
								}
							} else {
								!isFirstGradeBanned
								// !isFirstGradeBanned == isSecondGradeBanned
								// Because we are checking, if we applying first grade
								// on next iteration. And one of them is guaranteed banned.
							}
						} else {
							// casual in-progress behaviour of switcher
							!applyingFirstGrade
						}
						if (applyingFirstGrade) firstGradeCount++ else secondGradeCount++
					}
				}
				
				else -> return null
			}
		}
		
		val result = mutableListOf<RealizableBadMarks>()
		BadMarkStrategy.values().forEach { badMarkStrategy ->
			calculateGrades(badMarkStrategy)?.let { newCalculatedRealizable ->
				result.add(newCalculatedRealizable)
			}
		}
		return result
	}
	
	enum class BadMarkStrategy(private val mark1: Mark, private val mark2: Mark?) {
		FOURS(Mark.FOUR, null),
		FOURS_THREES(Mark.FOUR, Mark.THREE),
		THREES(Mark.THREE, null),
		THREES_TWOS(Mark.THREE, Mark.TWO),
		TWOS(Mark.TWO, null);
		
		
		fun getIntValuesOfGrades(): List<Int?> {
			return if (this.mark2?.value != null) {
				listOf(this.mark1.value, this.mark2.value)
			} else {
				listOf(this.mark1.value)
			}
		}
		
		operator fun component1(): Int = this.mark1.value!!
		
		operator fun component2(): Int? = this.mark2?.value
		
	}
	
	data class RealizableBadMarks(
		val strategy: BadMarkStrategy,
		val count: List<Int?>,
	)
	
	fun ClosedRange<LocalDate>.toList(limiter: Long? = null): List<LocalDate> {
		val dates = mutableListOf(this.start)
		var daysAdded = 1L
		while (daysAdded <= ChronoUnit.DAYS.between(this.start, this.endInclusive)) {
			dates.add(this.start.plusDays(daysAdded))
			daysAdded++
			if (limiter != null && limiter == daysAdded) break
		}
		return dates
	}
	
	inline fun <T> measurePerformanceInMS(logger: (Long, T) -> Unit, func: () -> T): T {
		val startTime = System.currentTimeMillis()
		val result: T = func.invoke()
		val endTime = System.currentTimeMillis()
		logger.invoke(endTime - startTime, result)
		return result
	}
	
	data class PeriodButton(
		val text: String,
		val callbackPeriod: EduPerformancePeriod,
	)
	
	@Composable
	fun getPeriodButtons(periodType: PeriodType, withYear: Boolean): List<PeriodButton> {
		val periods = if (periodType == PeriodType.TERMS) {
			Period.values().toList().dropLast(2)
		} else {
			Period.values().toList().drop(4)
		}.map { it.convertToEduPerformancePeriod() } as MutableList<EduPerformancePeriod>
		if (withYear) periods.add(EduPerformancePeriod.YEAR)
		
		val result = periods.map {
			if (it != EduPerformancePeriod.YEAR) {
				PeriodButton(
					text = stringArrayResource(R.array.ordinals)[it.value.toInt()] + " " + stringResource(
						when (periodType) {
							PeriodType.TERMS -> R.string.term
							PeriodType.SEMESTERS -> R.string.semester
						}
					),
					callbackPeriod = it
				)
			} else {
				PeriodButton(
					text = stringResource(R.string.year),
					callbackPeriod = it
				)
			}
		}
		return result
	}
	
	data class ScheduleCompareResult(
		val isNew: Boolean,
		val isDifferent: Boolean,
	)
	
	/**
	 * Returns a [Flow] whose values are generated by [transform] function that process the most
	 * recently emitted values by each flow.
	 */
	fun <T1, T2, T3, T4, T5, T6, R> combine(
		flow: Flow<T1>,
		flow2: Flow<T2>,
		flow3: Flow<T3>,
		flow4: Flow<T4>,
		flow5: Flow<T5>,
		flow6: Flow<T6>,
		transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
	): Flow<R> = combine(
		combine(flow, flow2, flow3, ::Triple),
		combine(flow4, flow5, flow6, ::Triple),
	) { t1, t2 ->
		transform(
			t1.first,
			t1.second,
			t1.third,
			t2.first,
			t2.second,
			t2.third,
		)
	}
}