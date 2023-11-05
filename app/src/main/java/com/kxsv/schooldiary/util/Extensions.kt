package com.kxsv.schooldiary.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.round

object Extensions {
	fun Double.stringRoundTo(decimals: Int): String {
		return String.format("%.${decimals}f", this, Locale.ENGLISH)
	}
	
	fun Double.roundTo(decimals: Int): Double {
		var multiplier = 1.0
		repeat(decimals) { multiplier *= 10 }
		return round(this * multiplier) / multiplier
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
}

