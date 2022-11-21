package ru.ac.uniyar.endpoints

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.BiDiLens
import org.http4k.lens.FormField
import org.http4k.lens.Invalid
import org.http4k.lens.Query
import org.http4k.lens.Validator
import org.http4k.lens.composite
import org.http4k.lens.int
import org.http4k.lens.localDate
import org.http4k.lens.long
import org.http4k.lens.nonEmptyString
import org.http4k.lens.string
import org.http4k.lens.webForm
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.filters.roleFilter
import ru.ac.uniyar.models.TravelFormVM
import ru.ac.uniyar.models.TravelGetVM
import ru.ac.uniyar.models.TravelsFormVM
import ru.ac.uniyar.models.template.ContextAwareViewRender
import ru.ac.uniyar.quires.ShipDb
import ru.ac.uniyar.quires.TravelDb
import ru.ac.uniyar.utils.BadRequestException
import ru.ac.uniyar.utils.Pageable
import ru.ac.uniyar.utils.RolePermission

fun travelRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    currentRolePermissionLens: BiDiLens<Request, RolePermission>,
    htmlView: ContextAwareViewRender,
    travel: TravelDb,
    ship: ShipDb
): RoutingHttpHandler =
    routes(
        "" bind roleFilter(currentRolePermissionLens, "travelCreate").then(travelPermissionRoute(currentUserLens, htmlView, travel, ship)),
        "/all" bind Method.GET to TravelsGet(htmlView, travel),
        "/{number}" bind Method.GET to TravelGet(htmlView, travel)
    )

fun travelPermissionRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    htmlView: ContextAwareViewRender,
    travel: TravelDb,
    ship: ShipDb
): RoutingHttpHandler =
    routes(
        "/create" bind Method.GET to TravelCreateGet(htmlView, ship),
        "/create" bind Method.POST to TravelCreatePost(currentUserLens, htmlView, travel)
    )

class TravelCreatePost(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: ContextAwareViewRender,
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
        var webForm = CreateTravelFormLens(request)
        if (startFormLens(webForm) >= endFormLens(webForm)) {
            val newErrors = webForm.errors + Invalid(startFormLens.meta.copy(description = "bad date"))
            webForm = webForm.copy(errors = newErrors)
        }
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
        return Response(Status.OK).with(htmlView(request) of TravelFormVM(webForm))
    }
}
class TravelCreateGet(
    private val htmlView: ContextAwareViewRender,
    private val ship: ShipDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val ships = ship.getAllShips()
        return Response(Status.OK).with(htmlView(request) of TravelFormVM(ships = ships))
    }
}

class TravelsGet(
    private val htmlView: ContextAwareViewRender,
    private val travel: TravelDb
) : HttpHandler {
    companion object {
        val pageable = Query.composite {
            Pageable(
                string().defaulted("sort", "no")(it),
                int().defaulted("page", 1)(it),
                int().defaulted("size", 2)(it),
                int().defaulted("maxpage", 20)(it),
                string().defaulted("filter", "all")(it)
            )
        }
        fun filterSwitch(filter: String): String {
            return when (filter) {
                "close" -> "Заполнено"
                "crew" -> "Идет набор команды"
                "end" -> "Закончено"
                "start" -> "В отплыве"
                "open" -> "Открыто для набора посетителей"
                "cancel" -> "Отменено"
                else -> {
                    throw BadRequestException("Не верный фильтр")
                }
            }
        }
    }
    override fun invoke(request: Request): Response {
        var pagination = pageable(request)
        if ((pagination.size <= 0) or (pagination.page <= 0)) {
            throw BadRequestException("Плохиец цифры")
        }
        val travelCount = if (pagination.filter != "all") travel.getCountTravel(filterSwitch(pagination.filter)) else travel.getCountTravel()

        val ftop = (travelCount / pagination.size)
        pagination.maxpage = if (travelCount % pagination.size == 0) ftop else ftop + 1
        if ((travelCount != 0) and (pagination.page == 1)) {
            if (pagination.page > pagination.maxpage) {
                throw BadRequestException("Плохой запрос")
            }
        }
        val travels = if (pagination.filter != "all") travel.getAllTravel(filterSwitch(pagination.filter), pagination.page, pagination.size, pagination.sort) else travel.getAllTravel(pagination.page, pagination.size, pagination.sort)
        return Response(Status.OK).with(htmlView(request) of TravelsFormVM(travels, pagination))
    }
}
class TravelGet(
    private val htmlView: ContextAwareViewRender,
    private val travel: TravelDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number").orEmpty().toLong()
        val travel = travel.fetch(id)
        return Response(Status.OK).with(htmlView(request) of TravelGetVM(travel))
    }
}
