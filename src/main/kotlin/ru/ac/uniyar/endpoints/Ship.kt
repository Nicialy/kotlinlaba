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
import org.http4k.lens.MultipartFormField
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.Validator
import org.http4k.lens.boolean
import org.http4k.lens.composite
import org.http4k.lens.int
import org.http4k.lens.long
import org.http4k.lens.multipartForm
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.filters.roleBiggerFilter
import ru.ac.uniyar.models.ShipGetCreateVM
import ru.ac.uniyar.models.ShipGetVM
import ru.ac.uniyar.models.ShipWTF
import ru.ac.uniyar.models.ShowErrorInfoVM
import ru.ac.uniyar.quires.ShipDb
import ru.ac.uniyar.utils.BadRequestException
import ru.ac.uniyar.utils.Pageable
import java.io.File

fun shipsRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    htmlView: BiDiBodyLens<ViewModel>,
    ship: ShipDb
): RoutingHttpHandler =
    routes(
        "/all" bind Method.GET to ShipsGet(currentUserLens, htmlView, ship),
        "/create" bind Method.GET to roleBiggerFilter(currentUserLens, 6).then(shipCreateGet(currentUserLens, htmlView)),
        "/create" bind Method.POST to roleBiggerFilter(currentUserLens, 6).then(ShipPost(currentUserLens, htmlView, ship)),
        "/{number}" bind Method.GET to ShipGet(currentUserLens, htmlView, ship),
        "/{number}/delete" bind Method.POST to roleBiggerFilter(currentUserLens, 6).then(ShipDel(currentUserLens, htmlView, ship))
    )

fun shipCreateGet(currentUserLens: BiDiLens<Request, DBUserEntity?>, htmlView: BiDiBodyLens<ViewModel>): HttpHandler = {
    val currentUser = currentUserLens(it)
    Response(Status.OK).with(htmlView of ShipGetCreateVM(currentUser))
}

class ShipPost(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val ship: ShipDb
) : HttpHandler {
    companion object {
        const val PathFile = "./src/main/resources/ru/ac/uniyar/models/public/photo/"
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
        val currentUser = currentUserLens(request)
        val receivedForm = shipMFormLens(request)
        val file = fileFormFiels(receivedForm)
        if (file != null) {
            // TODO error file need

            if (receivedForm.errors.isEmpty()) {
                File("./src/main/resources/ru/ac/uniyar/models/public/photo/${file.filename}").createNewFile()
                File("./src/main/resources/ru/ac/uniyar/models/public/photo/${file.filename}").let { file1 ->
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
        }
        return Response(Status.OK).with(htmlView of ShipGetCreateVM(currentUser, receivedForm))
    }
}

class ShipGet(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val ship: ShipDb
) : HttpHandler {
    companion object {
        private val idPathLens = Path.long()
    }
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val id = request.path("number").orEmpty().toLong()
        val shipget = ship.fetch(id)
            ?: return Response(Status.NOT_FOUND).with(htmlView of ShowErrorInfoVM(request.uri, currentUser, e.message))

        return Response(Status.OK).with(htmlView of ShipGetVM(currentUser, shipget))
    }
}

class ShipsGet(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val ship: ShipDb
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
        val ftop = (ship.getCountShip() / pagination.size)
        pagination.maxpage = if (ftop == 0) ftop else ftop + 1
        if (pagination.page > pagination.maxpage) {
            throw BadRequestException("Плохой запрос")
        }
        val shipGetList = ship.getAllShips(pagination.page, pagination.size)
        return Response(Status.OK).with(htmlView of ShipWTF(currentUser, shipGetList, pagination))
    }
}
class ShipDel(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val ship: ShipDb
) : HttpHandler {
    companion object {
        private val idPathLens = Path.long()
    }
    override fun invoke(request: Request): Response {
        val id = request.path("number").orEmpty().toLong()
        ship.shipDel(id)
        return Response(Status.NO_CONTENT).header("Location", "/ship/all")
    }
}
