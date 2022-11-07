package ru.ac.uniyar.utils

data class Pageable(val sortAscending: Boolean, val page: Int, val size: Int, var maxpage: Int = 0)
