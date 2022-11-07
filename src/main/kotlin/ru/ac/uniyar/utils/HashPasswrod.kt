package ru.ac.uniyar.utils

fun hashPassword(password: String): String {
    return Checksum(password.toByteArray()).toString()
}
