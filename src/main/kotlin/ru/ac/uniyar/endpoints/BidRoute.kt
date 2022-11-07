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
import org.http4k.lens.Validator
import org.http4k.lens.nonEmptyString
import org.http4k.lens.webForm
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.filters.roleBiggerFilter
import ru.ac.uniyar.models.BidVM
import ru.ac.uniyar.quires.BidDB
import ru.ac.uniyar.utils.BadRequestException
import ru.ac.uniyar.utils.NotAuthException

fun BidRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    htmlView: BiDiBodyLens<ViewModel>,
    bid: BidDB
): RoutingHttpHandler =
    routes(
        "/create" bind Method.GET to roleBiggerFilter(currentUserLens, 1).then(BidCreateGet(currentUserLens, htmlView, bid)),
        "/create" bind Method.POST to roleBiggerFilter(currentUserLens, 1).then(BidCreatePost(currentUserLens, htmlView, bid)),
        "/all" bind Method.GET to roleBiggerFilter(currentUserLens, 6).then(BidsGet(currentUserLens, htmlView, bid)),
        "/accept/{number}" bind Method.POST to roleBiggerFilter(currentUserLens, 6).then(BidAccept(currentUserLens, htmlView, bid)),
        "/cancel/{number}" bind Method.POST to roleBiggerFilter(currentUserLens, 6).then(BidCancel(currentUserLens, htmlView, bid)),
        "/delete/{number}" bind Method.POST to roleBiggerFilter(currentUserLens, 1).then(BidDelete(currentUserLens, htmlView, bid))
    )

class BidsGet(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val bid: BidDB
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val uriStr = request.uri.toString()
        val bidGetList = bid.getAllBid("В ожидании")
        return Response(Status.OK).with(htmlView of BidVM(currentUser, uriStr, bidGetList))
    }
}
class BidCreateGet(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val bid: BidDB
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val uriStr = request.uri.toString()
        return Response(Status.OK).with(htmlView of BidVM(currentUser, uriStr))
    }
}
class BidCreatePost(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val bid: BidDB
) : HttpHandler {
    companion object {
        private val descriptionFormLens = FormField.nonEmptyString().required("description")
        private val roleFormLens = FormField.nonEmptyString().required("role")
        private val CreateBidFormLens = Body.webForm(
            Validator.Feedback,
            descriptionFormLens,
            roleFormLens
        ).toLens()
    }
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val webForm = CreateBidFormLens(request)
        val uriStr = request.uri.toString()
        if (webForm.errors.isEmpty()) {
            bid.createBid(currentUser!!.id, roleFormLens(webForm), descriptionFormLens(webForm))
            return Response(Status.OK).header("Location", "/")
        }
        return Response(Status.OK).with(htmlView of BidVM(currentUser, uriStr))
    }
}
class BidAccept(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val bid: BidDB
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number")?.toLong() ?: throw BadRequestException("Не число")
        bid.acceptBid(id)
        return Response(Status.FOUND).header("Location", "/bid/all")
    }
}

class BidCancel(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val bid: BidDB
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val id = request.path("number").orEmpty().toLong()
        bid.cancelBid(id)
        return Response(Status.FOUND).header("Location", "/bid/all")
    }
}
class BidDelete(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val bid: BidDB
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val id = request.path("number").orEmpty().toLong()
        currentUser!!.id?.let { bid.delMyBid(it, id) } ?: NotAuthException("Не авторизован")
        return Response(Status.FOUND).header("Location", "/bid/all")
    }
}
