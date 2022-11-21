package ru.ac.uniyar.endpoints

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.lens.BiDiLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.filters.roleFilter
import ru.ac.uniyar.quires.TeamVisitorDB
import ru.ac.uniyar.utils.BadRequestException
import ru.ac.uniyar.utils.RolePermission

fun teamVisitorRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    teamVis: TeamVisitorDB,
    currentRolePermissionLens: BiDiLens<Request, RolePermission>
): RoutingHttpHandler =
    routes(
        "/create/{number}" bind Method.POST to roleFilter(currentRolePermissionLens, "travel").then(TvCreate(currentUserLens, teamVis))
    )

class TvCreate(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val teamVis: TeamVisitorDB
) : HttpHandler {

    override fun invoke(request: Request): Response {
        val travelId = request.path("number")?.toLong() ?: throw BadRequestException("Не число")
        val currentUser = currentUserLens(request)
        teamVis.goVisit(travelId, currentUser!!.id)
        return Response(Status.NO_CONTENT).header("Location", request.uri.toString())
    }
}
