package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity

sealed class AuthUserVM(
    val currentUser: DBUserEntity?,
    val num: Map<String?, Int> = mapOf<String?, Int>(
        null to 0,
        "Посетитель" to 1,
        "Матрос" to 2,
        "Повар" to 3,
        "Боцман" to 4,
        "Капитан" to 5,
        "Администратор" to 6
    )
) : ViewModel
