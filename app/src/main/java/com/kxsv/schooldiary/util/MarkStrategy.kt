package com.kxsv.schooldiary.util

import com.kxsv.schooldiary.data.util.Mark

enum class MarkStrategy(private val mark1: Mark, private val mark2: Mark?) {
	FIVES(Mark.FIVE, null),
	FIVES_FOURS(Mark.FIVE, Mark.FOUR),
	FOURS(Mark.FOUR, null),
	FOURS_THREES(Mark.FOUR, Mark.THREE),
	THREES(Mark.THREE, null),
	THREES_TWOS(Mark.THREE, Mark.TWO),
	TWOS(Mark.TWO, null);
	
	fun getIntValuesOfGrades(): List<Int>? {
		return if (this.mark1.value != null && this.mark2?.value != null) {
			listOf(this.mark1.value, this.mark2.value)
		} else if (this.mark1.value != null) {
			listOf(this.mark1.value)
		} else {
			null
		}
	}
	
	operator fun component1(): Int = this.mark1.value!!
	
	operator fun component2(): Int? = this.mark2?.value
	
}