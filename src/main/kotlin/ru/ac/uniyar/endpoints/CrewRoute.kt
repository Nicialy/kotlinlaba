package ru.ac.uniyar.endpoints

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.BiDiLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.quires.CrewDb
import ru.ac.uniyar.utils.BadRequestException

fun crewRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    crew: CrewDb
): RoutingHttpHandler =
    routes(
        "/delete/{number}" bind Method.POST to CrewDelete(currentUserLens, crew)
    )

class CrewDelete(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val crew: CrewDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number")?.toLong() ?: throw BadRequestException("Не число")
        val currentUser = currentUserLens(request)
        val travelId = crew.crewDelete(id, currentUser!!.id)
        return Response(Status.FOUND).header("Location", "/invitation/$travelId/create")
    }
}
