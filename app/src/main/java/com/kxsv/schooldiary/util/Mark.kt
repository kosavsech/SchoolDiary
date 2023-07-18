package com.kxsv.schooldiary.util

enum class Mark(val value: Int?, val letterValue: Char?) {
	ONE(1, null), TWO(2, null), THREE(3, null),
	FOUR(4, null), FIVE(5, null),
	ABSENT(null, 'Н'), ILL(null, 'Б');
	
	fun isNumeric(): Boolean = (this.value != null) && (this.letterValue == null)
	
	fun isLetter(): Boolean = (this.letterValue != null) && (this.value == null)
	
	fun getValue(): String = if (this.letterValue != null) {
		this.letterValue.toString()
	} else {
		this.value!!.toString()
	}
	
	
	companion object {
		fun fromInput(input: String): Mark = when (input) {
			"5" -> FIVE; "4" -> FOUR; "3" -> THREE; "2" -> TWO; "1" -> ONE
			"Н" -> ABSENT; "Б" -> ILL
			else -> throw IllegalArgumentException("Wrong mark input value($input)")
		}
	}
	
}