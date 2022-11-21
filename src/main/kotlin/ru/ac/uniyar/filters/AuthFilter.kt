package ru.ac.uniyar.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.cookie.cookie
import org.http4k.core.with
import org.http4k.lens.BiDiLens
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.database.UsersTable
import ru.ac.uniyar.utils.ADMIN
import ru.ac.uniyar.utils.ANOUTHERROLE
import ru.ac.uniyar.utils.BOATSWAIN
import ru.ac.uniyar.utils.CAPTAIN
import ru.ac.uniyar.utils.GUEST
import ru.ac.uniyar.utils.RolePermission
import ru.ac.uniyar.utils.VISITOR
import java.util.*
fun authFilter(
    currentUser: BiDiLens<Request, DBUserEntity?>,
    currentUserPermission: BiDiLens<Request, RolePermission>,
    fetchUserWithToken: FetchUserWithToken,
    jwtTools: JwtTools
): Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val requestWithUser = request.cookie("token")?.value?.let { token ->
            jwtTools.subject(token)
        }?.let { nickname ->
            fetchUserWithToken(nickname)
        }?.let { user ->
            request.with(currentUser of user, currentUserPermission of mapRole[user.role]!!)
        } ?: request.with(currentUserPermission of mapRole[null]!!)
        next(requestWithUser)
    }
}

class FetchUserWithToken(
    private val database: Database
) {
    operator fun invoke(nickname: String): DBUserEntity? {
        return database.sequenceOf(UsersTable).find { it.nickname eq nickname }
    }
}

val mapRole = mapOf<String?, RolePermission>(
    null to GUEST,
    "Посетитель" to VISITOR,
    "Матрос" to ANOUTHERROLE,
    "Повар" to ANOUTHERROLE,
    "Боцман" to BOATSWAIN,
    "Капитан" to CAPTAIN,
    "Администратор" to ADMIN
)
