package ru.ac.uniyar.endpoints

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiLens
import org.http4k.lens.FormField
import org.http4k.lens.Query
import org.http4k.lens.Validator
import org.http4k.lens.boolean
import org.http4k.lens.composite
import org.http4k.lens.int
import org.http4k.lens.localDate
import org.http4k.lens.long
import org.http4k.lens.nonEmptyString
import org.http4k.lens.webForm
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.filters.roleBiggerFilter
import ru.ac.uniyar.models.TravelFormVM
import ru.ac.uniyar.models.TravelGetVM
import ru.ac.uniyar.models.TravelsFormVM
import ru.ac.uniyar.quires.ShipDb
import ru.ac.uniyar.quires.TravelDb
import ru.ac.uniyar.utils.BadRequestException
import ru.ac.uniyar.utils.Pageable

fun travelRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    htmlView: BiDiBodyLens<ViewModel>,
    travel: TravelDb,
    ship: ShipDb
): RoutingHttpHandler =
    routes(
        "/create" bind Method.GET to roleBiggerFilter(currentUserLens, 5).then(TravelCreateGet(currentUserLens, htmlView, ship)),
        "/create" bind Method.POST to roleBiggerFilter(currentUserLens, 5).then(TravelCreatePost(currentUserLens, htmlView, ship, travel)),
        "/all" bind Method.GET to TravelsGet(currentUserLens, htmlView, travel),
        "/{number}" bind Method.GET to TravelGet(currentUserLens, htmlView, travel)
    )

class TravelCreatePost(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val ship: ShipDb,
    private val travel: TravelDb
) : HttpHandler {
    companion object {
        private val nameFormLens = FormField.nonEmptyString().required("name")
        private val descriptionFormLens = FormField.nonEmptyString().required("description")
        private val startFormLens = FormField.localDate().required("date_start")
        private val endFormLens = FormField.localDate().required("date_end")
        private val shipIdFormLens = FormField.long().required("ship_id")
        private val CreateTravelFormLens = Body.webForm(
            Validator.Feedback,
            nameFormLens,
            descriptionFormLens,
            startFormLens,
            endFormLens,
            shipIdFormLens
        ).toLens()
    }
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val webForm = CreateTravelFormLens(request)
        // TODO проверка на валиднуб дату
        if (webForm.errors.isEmpty()) {
            val id = travel.createTravel(
                currentUser!!.id!!,
                nameFormLens(webForm),
                shipIdFormLens(webForm),
                descriptionFormLens(webForm),
                startFormLens(webForm),
                endFormLens(webForm)
            )
            return Response(Status.FOUND).header("Location", "/travel/$id")
        }
        return Response(Status.OK).with(htmlView of TravelFormVM(currentUser, webForm))
    }
}
class TravelCreateGet(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val ship: ShipDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val ships = ship.getAllShips()
        return Response(Status.OK).with(htmlView of TravelFormVM(currentUser, ships = ships))
    }
}

class TravelsGet(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val travel: TravelDb
) : HttpHandler {
    companion object {
        val pageable = Query.composite {
            Pageable(
                boolean().defaulted("sortAscending", true)(it),
                int().defaulted("page", 1)(it),
                int().defaulted("size", 2)(it),
                int().defaulted("filter", 20)(it)
            )
        }
    }
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        var pagination = pageable(request)
        if ((pagination.size <= 0) or (pagination.page <= 0)) {
            throw BadRequestException("Плохиец цифры")
        }
        val ftop = (travel.getCountTravel() / pagination.size)
        pagination.maxpage = if (ftop == 0) ftop else ftop + 1
        if (pagination.page > pagination.maxpage) {
            throw BadRequestException("Плохой запрос")
        }
        val travels = travel.getAllTravel(pagination.page, pagination.size)
        return Response(Status.OK).with(htmlView of TravelsFormVM(currentUser, travels, pagination))
    }
}
class TravelGet(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val travel: TravelDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val id = request.path("number").orEmpty().toLong()
        val travel = travel.fetch(id)
        return Response(Status.OK).with(htmlView of TravelGetVM(currentUser, travel))
    }
}
