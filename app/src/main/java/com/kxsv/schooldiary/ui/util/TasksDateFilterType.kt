package com.kxsv.schooldiary.ui.util

import com.kxsv.schooldiary.R

enum class TasksDateFilterType {
	YESTERDAY,
	TODAY,
	TOMORROW,
	NEXT_WEEK,
	THIS_MONTH,
	NEXT_MONTH,
	ALL,
	SPECIFIC_DATE;
	
	
	fun getLocalisedStringId(): Int {
		return when (this) {
			YESTERDAY -> R.string.yesterday_filter
			TODAY -> R.string.today_filter
			TOMORROW -> R.string.tomorrow_filter
			NEXT_WEEK -> R.string.next_week_filter
			THIS_MONTH -> R.string.this_month_filter
			NEXT_MONTH -> R.string.next_month_filter
			ALL -> R.string.all_filter
			SPECIFIC_DATE -> R.string.specific_date
		}
	}
}