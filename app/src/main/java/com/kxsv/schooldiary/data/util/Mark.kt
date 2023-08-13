package com.kxsv.schooldiary.data.util

enum class Mark(val value: Int?, private val letterValue: Char?) {
	ONE(1, null), TWO(2, null), THREE(3, null),
	FOUR(4, null), FIVE(5, null),
	ABSENT(null, 'Н'), ILL(null, 'Б');
	
	companion object {
		fun getStringValueFrom(mark: Mark?): String = when {
			mark?.letterValue != null -> mark.letterValue.toString()
			mark?.value != null -> mark.value.toString()
			else -> "—"
		}
		
		fun fromInput(input: String): Mark? = when (input) {
			"5" -> FIVE
			"4" -> FOUR
			"3" -> THREE
			"2" -> TWO
			"1" -> ONE
			"Н" -> ABSENT
			"Не был" -> ABSENT
			"Б" -> ILL
			"Болел" -> ILL
			"" -> null
			"—" -> null
			else -> throw IllegalArgumentException("Wrong mark input value($input)")
		}
	}
}