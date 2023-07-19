package com.kxsv.schooldiary.util

fun <T> copyExclusively(targetItem: T, elements: Collection<T>): MutableList<T> {
	val newList = copyRefresh(elements)
	newList.remove(targetItem)
	return newList
}

fun <T> copyInclusively(targetItem: T, elements: Collection<T>): MutableList<T> {
	val newList = copyRefresh(elements)
	newList.add(targetItem)
	return newList
}

fun <T> copyRefresh(elements: Collection<T>): MutableList<T> {
	val newList: MutableList<T> = mutableListOf()
	newList.addAll(elements)
	return newList
}

fun <K, V> copyExclusively(targetItemKey: K, elements: Map<K, V>): Map<K, V> {
	val newMap: MutableMap<K, V> = mutableMapOf()
	newMap.putAll(elements)
	newMap.remove(targetItemKey)
	return newMap
}

fun <K, V> copyInclusively(targetItem: Map.Entry<K, V>, elements: Map<K, V>): Map<K, V> {
	val newMap: MutableMap<K, V> = mutableMapOf()
	newMap.putAll(elements)
	newMap[targetItem.key] = targetItem.value
	return newMap
}