package com.kxsv.schooldiary.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.Mark
import com.kxsv.schooldiary.data.util.user_preferences.Period
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.data.util.user_preferences.PeriodWithRange
import com.kxsv.schooldiary.util.Extensions.roundTo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
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
	val monthDayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d")
	val currentDate: LocalDate = LocalDate.now()
	private val holidays: List<String> = listOf(
		"11_6", "2_23", "3_8", "5_1", "5_9", "5_10",
	)
	
	fun isHoliday(date: LocalDate, termsPeriodRanges: List<ClosedRange<LocalDate>>): Boolean {
		if (date.dayOfWeek == DayOfWeek.SUNDAY) return true
		
		val isNonWorking = date in (holidays.map { periodRangeEntryToLocalDate(it) })
		return if (!isNonWorking) {
			termsPeriodRanges.all { date !in it }
		} else {
			true
		}
	}
	
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
	
	fun calculateStudyDaysUntilHolidaysStart(allPeriodRanges: List<PeriodWithRange>): Int? {
		val currentPeriodResult = calculateDaysUntilHolidaysStart(allPeriodRanges) ?: return null
		val termsPeriodsRanges = allPeriodRanges
			.filter { Period.getTypeByPeriod(it.period) == PeriodType.TERMS }
			.map {
				periodRangeEntryToLocalDate(it.range.start)..periodRangeEntryToLocalDate(it.range.end)
			}
		var counter = 0
		for (i in 0L until currentPeriodResult) {
			val date = currentDate.plusDays(i)
			if (!isHoliday(date, termsPeriodsRanges)) counter++
		}
		return counter
	}
	
	fun calculateStudyDaysUntilPeriodEnd(
		allPeriodRanges: List<PeriodWithRange>,
		periodType: PeriodType,
	): Int? {
		val currentPeriodResult =
			calculateDaysUntilPeriodEnd(allPeriodRanges, periodType) ?: return null
		val termsPeriodsRanges = allPeriodRanges
			.filter { Period.getTypeByPeriod(it.period) == PeriodType.TERMS }
			.map {
				periodRangeEntryToLocalDate(it.range.start)..periodRangeEntryToLocalDate(it.range.end)
			}
		var counter = 0
		for (i in 0L until currentPeriodResult) {
			val date = currentDate.plusDays(i)
			if (!isHoliday(date, termsPeriodsRanges)) counter++
		}
		return counter
	}
	
	fun calculateDaysUntilPeriodStart(
		allPeriodRanges: List<PeriodWithRange>, periodType: PeriodType,
	): Int? {
		if (allPeriodRanges.isEmpty()) return null
		
		val periodsRanges =
			allPeriodRanges.filter { Period.getTypeByPeriod(it.period) == periodType }
		
		val lastPeriodEnd =
			periodRangeEntryToLocalDate(periodsRanges.maxBy { it.period.ordinal }.range.end)
		if (currentDate.isAfter(lastPeriodEnd)) {
			val firstPeriodStart =
				periodRangeEntryToLocalDate(periodsRanges.minBy { it.period.ordinal }.range.start)
			return (currentDate.until(firstPeriodStart, ChronoUnit.DAYS) + 1).toInt()
		}
		
		var candidate: Pair<PeriodWithRange?, Long> = Pair(null, 366)
		periodsRanges.forEach { periodWithRange: PeriodWithRange ->
			val periodStart = periodRangeEntryToLocalDate(periodWithRange.range.start)
			val periodEnd = periodRangeEntryToLocalDate(periodWithRange.range.end)
			val periodTimeRange = periodStart..periodEnd
			
			if (currentDate !in periodTimeRange) {
				val daysUntilStart = currentDate.until(periodStart, ChronoUnit.DAYS)
				if (daysUntilStart < 0) return@forEach
				
				if (daysUntilStart < candidate.second) {
					candidate = Pair(periodWithRange, daysUntilStart)
				}
			} else {
				return null
			}
		}
		return if (candidate.first != null) {
			candidate.second.toInt()
		} else {
			null
		}
	}
	
	fun calculateDaysUntilHolidaysEnd(
		allPeriodRanges: List<PeriodWithRange>,
	): Int? = calculateDaysUntilPeriodStart(allPeriodRanges, PeriodType.TERMS)
	
	fun calculateDaysUntilPeriodEnd(
		allPeriodRanges: List<PeriodWithRange>, periodType: PeriodType,
	): Int? {
		if (allPeriodRanges.isEmpty()) return null
		
		val periodsRanges =
			allPeriodRanges.filter { Period.getTypeByPeriod(it.period) == periodType }
		
		val finalPeriodsEnd =
			periodRangeEntryToLocalDate(periodsRanges.maxBy { it.period.ordinal }.range.end)
		if (currentDate.isAfter(finalPeriodsEnd)) return null
		
		periodsRanges.forEach { periodWithRange: PeriodWithRange ->
			val periodStart = periodRangeEntryToLocalDate(periodWithRange.range.start)
			val periodEnd = periodRangeEntryToLocalDate(periodWithRange.range.end)
			val periodTimeRange = periodStart..periodEnd
			
			if (currentDate in periodTimeRange) {
				return (currentDate.until(periodEnd, ChronoUnit.DAYS) + 1).toInt()
			}
		}
		return null
	}
	
	fun calculateDaysUntilHolidaysStart(
		allPeriodRanges: List<PeriodWithRange>,
	): Int? = calculateDaysUntilPeriodEnd(allPeriodRanges, PeriodType.TERMS)
	
	/**
	 * Gets current period for ui.
	 * If current date is in range of period, then return it.
	 * Else return closest next period
	 *
	 * @param allPeriodRanges
	 * @param periodType
	 * @return
	 */
	fun getCurrentPeriodForUi(
		allPeriodRanges: List<PeriodWithRange>, periodType: PeriodType,
	): EduPerformancePeriod? {
		if (allPeriodRanges.isEmpty()) return null
		
		val periodsRanges =
			allPeriodRanges.filter { Period.getTypeByPeriod(it.period) == periodType }
		
		val lastPeriodEnd =
			periodRangeEntryToLocalDate(periodsRanges.maxBy { it.period.ordinal }.range.end)
		if (currentDate.isAfter(lastPeriodEnd)) return EduPerformancePeriod.FOURTH
		
		var candidate: Pair<PeriodWithRange?, Long> = Pair(null, 366)
		periodsRanges.forEach { periodWithRange ->
			val periodStart = periodRangeEntryToLocalDate(periodWithRange.range.start)
			val periodEnd = periodRangeEntryToLocalDate(periodWithRange.range.end)
			val periodTimeRange = periodStart..periodEnd
			
			if (currentDate !in periodTimeRange) {
				val daysUntilEnd = currentDate.until(periodEnd, ChronoUnit.DAYS) + 1
				if (daysUntilEnd < 0) return@forEach
				
				if (daysUntilEnd < candidate.second) {
					candidate = Pair(periodWithRange, daysUntilEnd)
				}
			} else {
				return periodWithRange.period.convertToEduPerformancePeriod()
			}
		}
		
		return if (candidate.first != null) {
			candidate.first!!.period.convertToEduPerformancePeriod()
		} else {
			null
		}
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
	 * Gets next [n] lessons indices after [startIndex].
	 * Used on list of lessons indices.
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
	
	private fun roundWithRule(x: Double, roundRule: Double): Double {
		val floored = floor(x)
		return if (x >= (floored + roundRule)) {
			ceil(x)
		} else {
			floored
		}
	}
	
	/**
	 * @param mark is mark with type [Double] so could consume any number of marks.
	 * @throws IllegalArgumentException if param [mark] > [5][Mark.FIVE] or [mark] < [2][Mark.TWO]
	 */
	fun getRuinBoundForMark(mark: Double, roundRule: Double): Double {
		if (mark > Mark.FIVE.value!! || mark < Mark.TWO.value!!) throw IllegalArgumentException("Non-existent mark")
		return roundWithRule(mark, roundRule) - 1 + roundRule
	}
	
	fun calculateMarksUntilTarget(
		roundRule: Double,
		target: Double,
		avgMark: Double,
		sum: Int,
		valueSum: Double,
	): List<CalculatedMark> {
		fun calculateGrades(strategy: MarkStrategy): CalculatedMark? {
			val strategyGrades = strategy.getIntValuesOfGrades() ?: return null
			
			when (strategyGrades.size) {
				1 -> {
					val strategyGrade = strategyGrades.component1()
					var gradeCount =
						if (strategyGrade < getRuinBoundForMark(avgMark, roundRule)) {
							Log.w(
								TAG,
								"calculateMarksUntilTarget: auto-skip of $strategyGrade strategy"
							)
							return null
						} else {
							1
						}
					
					while (true) {
						val valueOfAddedByGrade = strategyGrade * gradeCount
						
						val interimValue = valueSum + valueOfAddedByGrade
						val interimSum = sum + gradeCount
						val interimAvg = interimValue / interimSum
						
						if (interimAvg >= target || gradeCount == 50) {
							return if (gradeCount != 0 && gradeCount != 50) {
								val result = CalculatedMark(
									strategy = strategy,
									count = listOf(gradeCount),
									outcome = interimAvg.roundTo(2)
								)
								Log.d(
									TAG,
									"calculateMarksUntilTarget($strategyGrade) returned: $result"
								)
								result
							} else {
								null
							}
						}
						
						gradeCount++
					}
					
				}
				
				2 -> {
					val maxGrade = maxOf(strategyGrades.component1(), strategyGrades.component2())
					if (maxGrade < getRuinBoundForMark(avgMark, roundRule)) {
						Log.w(
							TAG,
							"calculateMarksUntilTarget: auto-skip of ${strategyGrades[0]} and ${strategyGrades[1]}"
						)
						return null
					}
					
					var applyingFirstGrade = false
					var firstGradeCount = 0
					var secondGradeCount = 1
					
					while (true) {
						val valueOfAddedByFirst = strategyGrades.component1() * firstGradeCount
						val valueOfAddedBySecond = strategyGrades.component2() * secondGradeCount
						
						val interimValue = valueSum + valueOfAddedByFirst + valueOfAddedBySecond
						val interimSum = sum + firstGradeCount + secondGradeCount
						val interimAvg = interimValue / interimSum
						
						applyingFirstGrade =
							if (interimAvg < target && firstGradeCount < 50 && secondGradeCount < 50) {
								!applyingFirstGrade
							} else {
								return if (firstGradeCount != 50 && secondGradeCount != 50 && secondGradeCount != 0) {
									val result = CalculatedMark(
										strategy = strategy,
										count = listOf(firstGradeCount, secondGradeCount),
										outcome = interimAvg.roundTo(2)
									)
									result
								} else {
									null
								}
							}
						if (applyingFirstGrade) firstGradeCount++ else secondGradeCount++
					}
				}
				
				else -> return null
			}
		}
		
		val result = mutableListOf<CalculatedMark>()
		MarkStrategy.values().dropLast(2).forEach { markStrategy ->
			calculateGrades(markStrategy)?.let {
				result.add(it)
			}
		}
		return result
	}
	
	fun calculateRealizableBadMarks(
		roundRule: Double,
		lowerBound: Double,
		avgMark: Double,
		sum: Int,
		valueSum: Double,
	): List<CalculatedMark> {
		fun calculateGrades(strategy: MarkStrategy): CalculatedMark? {
			var currentInProcessAvg = avgMark
			val strategyGrades = strategy.getIntValuesOfGrades() ?: return null
			
			when (strategyGrades.size) {
				1 -> {
					val strategyGrade = strategyGrades.component1()
					var gradeCount =
						if (strategyGrade >= lowerBound) {
							Log.w(TAG, "calculateGrades: auto-skip of $strategyGrade strategy")
							return null
						} else {
							1
						}
					
					while (true) {
						val valueOfAddedByGrade = strategyGrade * gradeCount
						
						val interimValue = valueSum + valueOfAddedByGrade
						val interimSum = sum + gradeCount
						val interimAvg = interimValue / interimSum
						
						if (interimAvg >= lowerBound && gradeCount <= 100) {
							Log.d(TAG, "currentInProcessAvg: $interimAvg > $lowerBound")
							currentInProcessAvg = interimAvg
						} else if ((gradeCount - 1) != 0) {
							val result = CalculatedMark(
								strategy = strategy,
								count = listOf(gradeCount - 1),
								outcome = currentInProcessAvg.roundTo(2)
							)
							Log.d(TAG, "calculateGrades(1) returned: $result")
							return result
						} else {
							return null
						}
						
						
						gradeCount++
					}
					
				}
				
				2 -> {
					val maxGrade = maxOf(strategyGrades.component1(), strategyGrades.component2())
					if (maxGrade >= lowerBound) {
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
						val valueOfAddedByFirst = strategyGrades.component1() * firstGradeCount
						val valueOfAddedBySecond = strategyGrades.component2() * secondGradeCount
						
						val interimValue = valueSum + valueOfAddedByFirst + valueOfAddedBySecond
						val interimSum = sum + firstGradeCount + secondGradeCount
						val interimAvg = interimValue / interimSum
						
						if (interimAvg >= lowerBound) {
							currentInProcessAvg = interimAvg
						} else {
							if (applyingFirstGrade) {
								firstGradeCount--
								isFirstGradeBanned = true
							} else {
								secondGradeCount--
								isSecondGradeBanned = true
							}
						}
						
						
						applyingFirstGrade =
							if ((isFirstGradeBanned || isSecondGradeBanned) || (firstGradeCount >= 100 || secondGradeCount >= 100)) {
								if (isFirstGradeBanned && isSecondGradeBanned || (firstGradeCount >= 100 || secondGradeCount >= 100)) {
									return if (firstGradeCount != 0 && secondGradeCount != 0) {
										val result = CalculatedMark(
											strategy = strategy,
											count = listOf(firstGradeCount, secondGradeCount),
											outcome = currentInProcessAvg.roundTo(2)
										)
										result
									} else {
										null
									}
								} else {
									!isFirstGradeBanned
									// !isFirstGradeBanned == isSecondGradeBanned
									// We are checking, are we applying first grade on next iteration.
									// i.e. we apply first grade always, unless it is banned.
									// And one of them is guaranteed banned.
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
		
		val result = mutableListOf<CalculatedMark>()
		MarkStrategy.values().drop(2).forEach { badMarkStrategy ->
			calculateGrades(badMarkStrategy)?.let { newCalculatedRealizable ->
				result.add(newCalculatedRealizable)
			}
		}
		return result
	}
	
	inline fun <T> measurePerformanceInMS(logger: (Long, T) -> Unit, func: () -> T): T {
		val startTime = System.currentTimeMillis()
		val result: T = func.invoke()
		val endTime = System.currentTimeMillis()
		logger.invoke(endTime - startTime, result)
		return result
	}
	
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