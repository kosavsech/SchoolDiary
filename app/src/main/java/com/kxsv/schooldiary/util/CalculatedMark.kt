package com.kxsv.schooldiary.util

data class CalculatedMark(
	val strategy: MarkStrategy,
	val count: List<Int>,
	val outcome: Double,
)