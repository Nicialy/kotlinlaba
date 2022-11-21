package ru.ac.uniyar.utils

data class Pageable(val sort: String, val page: Int, val size: Int, var maxpage: Int = 0, val filter: String = "")
