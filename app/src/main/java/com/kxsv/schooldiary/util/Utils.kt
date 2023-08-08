package com.kxsv.schooldiary.util

import android.util.Log
import com.kxsv.schooldiary.data.local.features.subject.SubjectEntity
import com.kxsv.schooldiary.data.local.features.time_pattern.pattern_stroke.PatternStrokeEntity
import com.kxsv.schooldiary.util.ui.EduPerformancePeriod
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor

private const val TAG = "Utils"
const val ROUND_RULE = 0.6

object Utils {
	val currentDate = LocalDate.of(2023, 5, 15)
	
	fun List<PatternStrokeEntity>.getIndexByTime(currentTime: LocalTime): Int? {
		val result = this.firstOrNull {
			currentTime.isAfter(it.startTime) && currentTime.isBefore(it.endTime)
		}
		return result?.index
	}
	
	fun List<PatternStrokeEntity>.getIndexOfClosestLessonToTime(currentTime: LocalTime): Int? {
		val result = this.firstOrNull {
			currentTime.isBefore(it.startTime)
		}
		return result?.index
	}
	
	/**
	 * Gets next [n] lessons after [index]
	 *
	 * @param n how much lessons to take
	 * @param index after which index
	 * @return
	 */
	fun Map<Int, SubjectEntity>.getNextLessonsAfterIndex(n: Int, index: Int?): List<Int>? {
		val subList = if (index != null) {
			this.filter { it.key > index }.keys.toList()
		} else {
			this.keys.toList()
		}
		if (subList.isEmpty()) return null
		
		val result = mutableListOf<Int>()
		subList.forEach {
			if (result.size == n) return@forEach
			result.add(it)
		}
		
		return result
	}
	
	fun timestampToLocalDate(value: Long?): LocalDate? = value?.let {
		Instant.ofEpochSecond(it).atZone(ZoneId.of("Europe/Moscow")).toLocalDate()
	}
	
	fun localDateToTimestamp(date: LocalDate?): Long? =
		date?.atStartOfDay(ZoneId.of("Europe/Moscow"))?.toEpochSecond()
	
	fun Double.stringRoundTo(n: Int): String {
		return String.format("%.${n}f", this, Locale.ENGLISH)
	}
	
	fun Double.roundTo(n: Int): Double {
		return String.format("%.${n}f", this, Locale.ENGLISH).toDouble()
	}
	
	fun roundWithRule(x: Double): Double {
		val floored = floor(x)
		return if (x >= (floored + ROUND_RULE)) {
			ceil(x)
		} else {
			floored
		}
	}
	
	/**
	 * @param x which is [grade][Mark.value]
	 * @throws IllegalArgumentException if param [x] > [5][Mark.FIVE] or [x] < [2][Mark.TWO]
	 */
	fun getLowerBoundOfGrade(x: Int): Double {
		if (x > Mark.FIVE.value!! || x < Mark.TWO.value!!) throw IllegalArgumentException("Non-existent grade")
		return x - 1 + ROUND_RULE
	}
	
	/**
	 * @param x is mark with type [Double] so could consume any number of marks.
	 * @throws IllegalArgumentException if param [x] > [5][Mark.FIVE] or [x] < [2][Mark.TWO]
	 */
	fun getLowerBoundForMark(x: Double): Double {
		if (x > Mark.FIVE.value!! || x < Mark.TWO.value!!) throw IllegalArgumentException("Non-existent mark")
		return roundWithRule(x) - 1 + ROUND_RULE
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
						if (grade.first() >= roundWithRule(processAvg)) {
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
					if (maxGrade >= roundWithRule(processAvg)) {
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
				PeriodButton(text = "First term", callbackPeriod = EduPerformancePeriod.FIRST_TERM),
				PeriodButton(
					text = "Second term",
					callbackPeriod = EduPerformancePeriod.SECOND_TERM
				),
				PeriodButton(text = "Third term", callbackPeriod = EduPerformancePeriod.THIRD_TERM),
				PeriodButton(
					text = "Fourth term",
					callbackPeriod = EduPerformancePeriod.FOURTH_TERM
				),
			)
			val all = listOf(
				PeriodButton(text = "First term", callbackPeriod = EduPerformancePeriod.FIRST_TERM),
				PeriodButton(
					text = "Second term",
					callbackPeriod = EduPerformancePeriod.SECOND_TERM
				),
				PeriodButton(text = "Third term", callbackPeriod = EduPerformancePeriod.THIRD_TERM),
				PeriodButton(
					text = "Fourth term",
					callbackPeriod = EduPerformancePeriod.FOURTH_TERM
				),
				PeriodButton(text = "Year", callbackPeriod = EduPerformancePeriod.YEAR_PERIOD),
			)
		}
	}
	
	data class ScheduleCompareResult(
		val isNew: Boolean,
		val isDifferent: Boolean,
	)
}