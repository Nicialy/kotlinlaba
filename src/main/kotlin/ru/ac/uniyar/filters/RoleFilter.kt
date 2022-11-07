package ru.ac.uniyar.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.lens.BiDiLens
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.utils.ForbiddenException

val num = mapOf<String?, Int>(
    null to 0,
    "Посетитель" to 1,
    "Матрос" to 2,
    "Повар" to 3,
    "Боцман" to 4,
    "Капитан" to 5,
    "Администратор" to 6
)

fun roleBiggerFilter(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    keyGo: Int
): Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val currentUser = currentUserLens(request)

        if (num[currentUser?.role]!! >= keyGo) {
            next(request)
        } else {
            throw ForbiddenException("Доступ Закрыт")
        }
    }
}

fun oneRoleFilter(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    keySort: Int
): Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val currentUser = currentUserLens(request)
        if (num[currentUser?.role]!! == keySort) {
            next(request)
        } else {
            throw ForbiddenException("Нет доступа")
        }
    }
}
