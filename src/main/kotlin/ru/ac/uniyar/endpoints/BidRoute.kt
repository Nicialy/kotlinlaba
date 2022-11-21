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
import org.http4k.lens.Query
import org.http4k.lens.Validator
import org.http4k.lens.composite
import org.http4k.lens.int
import org.http4k.lens.nonEmptyString
import org.http4k.lens.string
import org.http4k.lens.webForm
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.filters.roleFilter
import ru.ac.uniyar.models.BidCreateVM
import ru.ac.uniyar.models.BidVM
import ru.ac.uniyar.models.template.ContextAwareViewRender
import ru.ac.uniyar.quires.BidDB
import ru.ac.uniyar.utils.BadRequestException
import ru.ac.uniyar.utils.Pageable
import ru.ac.uniyar.utils.RolePermission

fun BidRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    currentRolePermissionLens: BiDiLens<Request, RolePermission>,
    htmlView: ContextAwareViewRender,
    bid: BidDB
): RoutingHttpHandler =
    routes(
        "/create" bind Method.GET to roleFilter(currentRolePermissionLens, "bidCreate").then(BidCreateGet(htmlView)),
        "/create" bind Method.POST to roleFilter(currentRolePermissionLens, "bidCreate").then(BidCreatePost(currentUserLens, htmlView, bid)),
        "/all" bind Method.GET to roleFilter(currentRolePermissionLens, "bidAccept").then(BidsGet(htmlView, bid)),
        "/accept/{number}" bind Method.POST to roleFilter(currentRolePermissionLens, "bidAccept").then(BidAccept(bid)),
        "/cancel/{number}" bind Method.POST to roleFilter(currentRolePermissionLens, "bidAccept").then(BidCancel(bid)),
        "/delete/{number}" bind Method.POST to roleFilter(currentRolePermissionLens, "bidCreate").then(BidDelete(currentUserLens, bid))
    )

class BidsGet(
    private val htmlView: ContextAwareViewRender,
    private val bid: BidDB
) : HttpHandler {
    companion object {
        val pageable = Query.composite {
            Pageable(
                string().defaulted("sortAscending", "no")(it),
                int().defaulted("page", 1)(it),
                int().defaulted("size", 2)(it),
                int().defaulted("maxpage", 0)(it),
                string().defaulted("filter", "new")(it)
            )
        }
        fun filterSwitch(filter: String): String {
            return when (filter) {
                "new" -> "В ожидании"
                "accept" -> "Одобрено"
                "cancel" -> "Отказано"
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
        val filter = filterSwitch(pagination.filter)
        val bidCount = bid.getCountBid(filter)
        val ftop = (bidCount / pagination.size)
        pagination.maxpage = if (bid.getCountBid(filter) % pagination.size == 0) ftop else ftop + 1
        if ((bidCount != 0) and (pagination.page == 1)) {
            if (pagination.page > pagination.maxpage) {
                throw BadRequestException("Плохой запрос")
            }
        }
        val bidGetList = bid.getAllBid(filter, pagination.page, pagination.size)
        return Response(Status.OK).with(htmlView(request) of BidVM(bidGetList, pagination))
    }
}
class BidCreateGet(
    private val htmlView: ContextAwareViewRender,
) : HttpHandler {
    override fun invoke(request: Request): Response {
        return Response(Status.OK).with(htmlView(request) of BidCreateVM())
    }
}
class BidCreatePost(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: ContextAwareViewRender,
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
        if (webForm.errors.isEmpty()) {
            bid.createBid(currentUser!!.id, roleFormLens(webForm), descriptionFormLens(webForm))
            return Response(Status.FOUND).header("Location", "/")
        }
        return Response(Status.OK).with(htmlView(request) of BidCreateVM())
    }
}
class BidAccept(
    private val bid: BidDB
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number")?.toLong() ?: throw BadRequestException("Не число")
        bid.acceptBid(id)
        return Response(Status.FOUND).header("Location", "/bid/all")
    }
}

class BidCancel(
    private val bid: BidDB
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number").orEmpty().toLong()
        bid.cancelBid(id)
        return Response(Status.FOUND).header("Location", "/bid/all")
    }
}
class BidDelete(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val bid: BidDB
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val id = request.path("number").orEmpty().toLong()
        currentUser!!.id?.let { bid.delMyBid(it, id) }
        return Response(Status.FOUND).header("Location", "/bid/all")
    }
}
