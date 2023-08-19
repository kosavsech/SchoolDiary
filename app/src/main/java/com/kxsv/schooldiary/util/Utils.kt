package com.kxsv.schooldiary.util

import android.util.Log
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.data.util.EduPerformancePeriod
import com.kxsv.schooldiary.data.util.Mark
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

const val MINUTES_PER_HOUR = 60
const val SECONDS_PER_MINUTE = 60
const val SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR

object Utils {
	val currentDate: LocalDate = LocalDate.of(2023, 5, 15)
	
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
			Log.d(TAG, "getNextLessonsIndices: startIndex == null")
			return null
		}
		if (this.isEmpty()) return null
		
		Log.d(TAG, "getNextLessonsIndices: this: $this")
		val indicesSublist = this.filter { it >= startIndex }.toList()
		Log.d(TAG, "getNextLessonsIndices: indicesSublist: $indicesSublist")
		if (indicesSublist.isEmpty()) {
			Log.d(TAG, "getNextLessonsIndices: indicesSublist.isEmpty()")
			return null
		}
		
		val result = mutableListOf<Int>()
		indicesSublist.forEach {
			if (result.size == n) return result
			result.add(it)
		}
		Log.d(TAG, "getNextLessonsIndices() returned: $result")
		return result
	}
	
	fun fromLocalTime(localTime: LocalTime?): String? =
		localTime?.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH))
	
	
	fun localDateToPeriodRangeEntry(date: LocalDate): String =
		"${date.monthValue}_${date.dayOfMonth}"
	
	fun periodRangeEntryToLocalDate(value: String): LocalDate = value.let {
		val monthValue = it.split("_")[0].toInt()
		val dayOfMonth = it.split("_")[1].toInt()
		
		return LocalDate.of(LocalDate.now().year, monthValue, dayOfMonth)
	}
	
	fun timestampToLocalDate(value: Long?): LocalDate? =
		value?.let { Instant.ofEpochSecond(it).atZone(ZoneId.of("Europe/Moscow")).toLocalDate() }
	
	fun localDateToTimestamp(date: LocalDate?): Long? =
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
	
	fun calculateRealizableBadMarks(
		roundRule: Double,
		lowerBound: Double,
		avgMark: Double,
		sum: Int,
		valueSum: Double,
	): Map<String, Map<String, Int?>?> {
		fun calculateGrades(vararg grade: Int): Map<String, Int?>? {
			var processAvg = avgMark
			when (grade.size) {
				1 -> {
					var gradeCount: Int =
						if (grade.first() >= roundWithRule(processAvg, roundRule)) {
							Log.w(TAG, "calculateGrades: auto-skip of ${grade[0]}")
							return null
						} else {
							0
						}
					
					while (processAvg > lowerBound && gradeCount != 66) {
						processAvg =
							(valueSum + grade.first() * (gradeCount + 1)) / (sum + (gradeCount + 1))
						if (processAvg < lowerBound) {
							// early quit because can't stand gradeCount increase
							return if (gradeCount == 0) {
								Log.w(
									TAG,
									"calculateGrades: semi-auto-skip on fail after first try ${grade[0]}"
								)
								null
							} else {
								mapOf(Pair(grade.first().toString(), gradeCount))
							}
						}
						gradeCount++
					}
					return if (gradeCount == 0) {
						null
					} else {
						mapOf(Pair(grade.first().toString(), gradeCount))
					}
				}
				
				2 -> {
					val maxGrade = maxOf(grade[0], grade[1])
					if (maxGrade >= roundWithRule(processAvg, roundRule)) {
						Log.w(TAG, "calculateGrades: auto-skip of ${grade[0]} and ${grade[1]}")
						return null
					}
					
					var firstGradeCount = 0
					var secondGradeCount = 0
					var applyingFirstGrade = true
					var isSecondGradeBanned = false
					var isFirstGradeBanned = false
					
					while (processAvg > lowerBound && firstGradeCount != 66) {
						if (applyingFirstGrade) firstGradeCount++ else secondGradeCount++
						
						val tryPassCheckValue =
							(valueSum + (grade[0] * firstGradeCount) + (grade[1] * secondGradeCount)) /
									(sum + firstGradeCount + secondGradeCount)
						
						if (tryPassCheckValue < lowerBound) {
							if (applyingFirstGrade) {
								firstGradeCount--; isFirstGradeBanned = true
							} else {
								secondGradeCount--; isSecondGradeBanned = true
							}
						} else {
							processAvg = tryPassCheckValue
						}
						
						applyingFirstGrade = if (isFirstGradeBanned || isSecondGradeBanned) {
							if (isFirstGradeBanned && isSecondGradeBanned) {
								return if (firstGradeCount == 0 || secondGradeCount == 0) {
									null
								} else {
									mapOf(
										Pair(grade[0].toString(), firstGradeCount),
										Pair(grade[1].toString(), secondGradeCount)
									)
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
					}
					return if (firstGradeCount == 0 || secondGradeCount == 0) {
						null
					} else {
						mapOf(
							Pair(grade[0].toString(), firstGradeCount),
							Pair(grade[1].toString(), secondGradeCount)
						)
					}
					
				}
				
				else -> return null
			}
		}
		
		val result: MutableMap<String, Map<String, Int?>?> = mutableMapOf()
		
		result["4_"] = calculateGrades(4)
		result["4_3"] = calculateGrades(4, 3)
		result["3_4"] = calculateGrades(3, 4)
		result["3_"] = calculateGrades(3)
		result["3_2"] = calculateGrades(3, 2)
		result["2_3"] = calculateGrades(2, 3)
		result["2_"] = calculateGrades(2)
		
		Log.d(TAG, "calculateRealizableBadMarks() returned: $result")
		return result
	}
	
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
	) {
		companion object {
			val allTerms = listOf(
				PeriodButton(text = "First term", callbackPeriod = EduPerformancePeriod.FIRST),
				PeriodButton(
					text = "Second term",
					callbackPeriod = EduPerformancePeriod.SECOND
				),
				PeriodButton(text = "Third term", callbackPeriod = EduPerformancePeriod.THIRD),
				PeriodButton(
					text = "Fourth term",
					callbackPeriod = EduPerformancePeriod.FOURTH
				),
			)
			val all = listOf(
				PeriodButton(text = "First term", callbackPeriod = EduPerformancePeriod.FIRST),
				PeriodButton(
					text = "Second term",
					callbackPeriod = EduPerformancePeriod.SECOND
				),
				PeriodButton(text = "Third term", callbackPeriod = EduPerformancePeriod.THIRD),
				PeriodButton(
					text = "Fourth term",
					callbackPeriod = EduPerformancePeriod.FOURTH
				),
				PeriodButton(text = "Year", callbackPeriod = EduPerformancePeriod.YEAR),
			)
		}
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