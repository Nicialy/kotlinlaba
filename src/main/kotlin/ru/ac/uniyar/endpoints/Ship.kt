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
import org.http4k.lens.MultipartFormField
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Query
import org.http4k.lens.Validator
import org.http4k.lens.composite
import org.http4k.lens.int
import org.http4k.lens.multipartForm
import org.http4k.lens.string
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.filters.roleFilter
import ru.ac.uniyar.models.ShipGetCreateVM
import ru.ac.uniyar.models.ShipGetVM
import ru.ac.uniyar.models.ShipWTF
import ru.ac.uniyar.models.template.ContextAwareViewRender
import ru.ac.uniyar.quires.ShipDb
import ru.ac.uniyar.utils.BadRequestException
import ru.ac.uniyar.utils.Pageable
import ru.ac.uniyar.utils.RolePermission
import java.io.File

fun shipsRoute(
    htmlView: ContextAwareViewRender,
    currentRolePermissionLens: BiDiLens<Request, RolePermission>,
    ship: ShipDb
): RoutingHttpHandler =
    routes(
        "" bind roleFilter(currentRolePermissionLens, "shipAdd").then(shipsPermissionRoute(htmlView, ship)),
        "/all" bind Method.GET to ShipsGet(htmlView, ship),
        "/{number}" bind Method.GET to ShipGet(htmlView, ship)
    )
fun shipsPermissionRoute(
    htmlView: ContextAwareViewRender,
    ship: ShipDb
): RoutingHttpHandler =
    routes(
        "/create" bind Method.GET to shipCreateGet(htmlView),
        "/create" bind Method.POST to ShipPost(htmlView, ship),
        "/{number}/delete" bind Method.POST to ShipDel(ship)
    )

fun shipCreateGet(htmlView: ContextAwareViewRender): HttpHandler = {
    Response(Status.OK).with(htmlView(it) of ShipGetCreateVM())
}

class ShipPost(
    private val htmlView: ContextAwareViewRender,
    private val ship: ShipDb
) : HttpHandler {
    companion object {
        private val countFormLens = MultipartFormField.string().map(String::toInt).required("count")
        private val descriptionFormLens = MultipartFormField.string().required("description")
        private val nameMFormField = MultipartFormField.string().required("name")
        private val fileFormFiels = MultipartFormFile.required("file")
        private val shipMFormLens = Body.multipartForm(
            Validator.Feedback,
            nameMFormField,
            countFormLens,
            descriptionFormLens,
            fileFormFiels
        ).toLens()
    }
    override fun invoke(request: Request): Response {
        val receivedForm = shipMFormLens(request)
        val file = fileFormFiels(receivedForm)
        if (receivedForm.errors.isEmpty()) {
            File("./photo/${file.filename}").createNewFile()
            File("./photo/${file.filename}").let { file1 ->
                file1.outputStream().use {
                    file.content.copyTo(it)
                }
            }
            val id = ship.createShip(
                nameMFormField(receivedForm),
                descriptionFormLens(receivedForm),
                file.filename,
                countFormLens(receivedForm)
            )
            return Response(Status.FOUND).header("Location", "/ship/$id")
        }
        return Response(Status.OK).with(htmlView(request) of ShipGetCreateVM(receivedForm))
    }
}

class ShipGet(
    private val htmlView: ContextAwareViewRender,
    private val ship: ShipDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number").orEmpty().toLong()
        val shipGet = ship.fetch(id)
        return Response(Status.OK).with(htmlView(request) of ShipGetVM(shipGet))
    }
}

class ShipsGet(
    private val htmlView: ContextAwareViewRender,
    private val ship: ShipDb
) : HttpHandler {
    companion object {
        val pageable = Query.composite {
            Pageable(
                string().defaulted("sortAscending", "no")(it),
                int().defaulted("page", 1)(it),
                int().defaulted("size", 2)(it),
                int().defaulted("filter", 20)(it)
            )
        }
    }
    override fun invoke(request: Request): Response {
        var pagination = pageable(request)
        if ((pagination.size <= 0) or (pagination.page <= 0)) {
            throw BadRequestException("Плохиец цифры")
        }
        val shipCount = ship.getCountShip()
        val ftop = (shipCount / pagination.size)
        pagination.maxpage = if (shipCount % pagination.size == 0) ftop else ftop + 1
        if (pagination.page > pagination.maxpage) {
            throw BadRequestException("Плохой запрос")
        }
        val shipGetList = ship.getAllShips(pagination.page, pagination.size)
        return Response(Status.OK).with(htmlView(request) of ShipWTF(shipGetList, pagination))
    }
}
class ShipDel(
    private val ship: ShipDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number").orEmpty().toLong()
        ship.shipDel(id)
        return Response(Status.FOUND).header("Location", "/ship/all")
    }
}
