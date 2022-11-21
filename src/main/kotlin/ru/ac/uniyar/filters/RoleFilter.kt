package ru.ac.uniyar.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.lens.BiDiLens
import ru.ac.uniyar.utils.ForbiddenException
import ru.ac.uniyar.utils.MyRoles
import ru.ac.uniyar.utils.RolePermission

val role = MyRoles()
fun roleFilter(
    currentUserPermissionLens: BiDiLens<Request, RolePermission>,
    keyGo: String
): Filter = Filter { next: HttpHandler ->
    { request: Request ->
        val permission = currentUserPermissionLens(request)
        if (role.mapHandler[keyGo]!!.call(permission)) {
            next(request)
        } else {
            throw ForbiddenException("Доступ Закрыт")
        }
    }
}
