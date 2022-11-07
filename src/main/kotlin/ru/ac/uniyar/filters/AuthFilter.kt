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

fun authFilter(
    currentUser: BiDiLens<Request, DBUserEntity?>,
    fetchUserWithToken: FetchUserWithToken,
    jwtTools: JwtTools
): Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val requestWithUser = request.cookie("token")?.value?.let { token ->
            jwtTools.subject(token)
        }?.let { nickname ->
            fetchUserWithToken(nickname)
        }?.let { user ->
            request.with(currentUser of user)
        } ?: request
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
