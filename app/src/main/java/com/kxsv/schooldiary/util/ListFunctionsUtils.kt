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