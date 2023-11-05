package com.kxsv.schooldiary.ui.util

import androidx.annotation.StringRes
import com.kxsv.schooldiary.R
import com.kxsv.schooldiary.data.util.user_preferences.PeriodType
import com.kxsv.schooldiary.data.util.user_preferences.PeriodWithRange
import com.kxsv.schooldiary.util.Utils

enum class DaysCounterType(@StringRes val textRes: Int) {
	StudyDaysUntilHolidaysStart(R.string.study_days_until_holidays) {
		override fun calculate(
			allPeriodRanges: List<PeriodWithRange>,
			periodType: PeriodType?,
		): Int? {
			return Utils.calculateStudyDaysUntilHolidaysStart(allPeriodRanges)
		}
	},
	DaysUntilHolidaysStart(R.string.days_until_holidays) {
		override fun calculate(
			allPeriodRanges: List<PeriodWithRange>,
			periodType: PeriodType?,
		): Int? {
			return Utils.calculateDaysUntilHolidaysStart(allPeriodRanges)
		}
	},
	DaysUntilHolidaysEnd(R.string.days_until_holidays_end) {
		override fun calculate(
			allPeriodRanges: List<PeriodWithRange>,
			periodType: PeriodType?,
		): Int? {
			return Utils.calculateDaysUntilHolidaysEnd(allPeriodRanges)
		}
	},
	StudyDaysUntilPeriodEnd(R.string.study_days_until_period_end) {
		override fun calculate(
			allPeriodRanges: List<PeriodWithRange>,
			periodType: PeriodType?,
		): Int? {
			return periodType?.let {
				Utils.calculateStudyDaysUntilPeriodEnd(
					allPeriodRanges = allPeriodRanges,
					periodType = it
				)
			}
		}
	},
	DaysUntilPeriodEnd(R.string.days_until_period_end) {
		override fun calculate(
			allPeriodRanges: List<PeriodWithRange>,
			periodType: PeriodType?,
		): Int? {
			return periodType?.let {
				Utils.calculateDaysUntilPeriodEnd(
					allPeriodRanges = allPeriodRanges,
					periodType = it
				)
			}
		}
	};
	
	abstract fun calculate(
		allPeriodRanges: List<PeriodWithRange>,
		periodType: PeriodType? = null,
	): Int?
}