package ru.ac.uniyar.endpoints

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.BiDiLens
import org.http4k.lens.Query
import org.http4k.lens.composite
import org.http4k.lens.int
import org.http4k.lens.string
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.models.InvitationCreateVM
import ru.ac.uniyar.models.InvitationVM
import ru.ac.uniyar.models.template.ContextAwareViewRender
import ru.ac.uniyar.quires.CrewDb
import ru.ac.uniyar.quires.InvitationDb
import ru.ac.uniyar.quires.UsersDb
import ru.ac.uniyar.utils.BadRequestException
import ru.ac.uniyar.utils.Pageable

fun InvitationRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    htmlView: ContextAwareViewRender,
    crew: CrewDb,
    invitation: InvitationDb,
    user: UsersDb
): RoutingHttpHandler =
    routes(
        // Капитан Админ
        "/{number}/create" bind Method.GET to InvitationCreateGet(htmlView, crew, invitation, user),
        "/{number}/create/{user_id}" bind Method.POST to InvitationCreatePost(invitation),
        // все
        "/my" bind Method.GET to InvitationMy(currentUserLens, htmlView, invitation),
        "/accept/{number}" bind Method.POST to InvitationAccept(currentUserLens, invitation),
        "/cancel/{number}" bind Method.POST to InvitationCancel(currentUserLens, invitation)
    )

class InvitationMy(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: ContextAwareViewRender,
    private val invitation: InvitationDb
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
                "new" -> "Не ответил"
                "accept" -> "Принял"
                "cancel" -> "Отказал"
                else -> {
                    throw BadRequestException("Не верный фильтр")
                }
            }
        }
    }
    override fun invoke(request: Request): Response {
        var pagination = pageable(request)
        val user = currentUserLens(request)
        if ((pagination.size <= 0) or (pagination.page <= 0)) {
            throw BadRequestException("Плохие цифры")
        }
        val filter = filterSwitch(pagination.filter)
        val invitationCount = invitation.getMyCountInvitation(user!!, filter)
        val ftop = (invitationCount / pagination.size)
        pagination.maxpage = if (invitationCount % pagination.size == 0) ftop else ftop + 1
        if ((invitationCount != 0) and (pagination.page == 1)) {
            if (pagination.page > pagination.maxpage) {
                throw BadRequestException("Плохой запрос")
            }
        }
        val invitationUser = invitation.myInvitation(user!!.id, pagination.page, pagination.size, filter)
        return Response(Status.OK).with(htmlView(request) of InvitationVM(invitationUser, pagination))
    }
}

class InvitationCreateGet(
    private val htmlView: ContextAwareViewRender,
    private val crew: CrewDb,
    private val invitation: InvitationDb,
    private val userCanInvite: UsersDb

) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number").orEmpty().toLong()
        val crewUsers = crew.getCrewTravel(id)
        val invitationUsers = invitation.travelInvitation(id)
        val usersCan = userCanInvite.fetchUsersWithRole(id)
        return Response(Status.OK).with(htmlView(request) of InvitationCreateVM(crewUsers, usersCan, invitationUsers))
    }
}

class InvitationCreatePost(
    private val invitation: InvitationDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val travelId = request.path("number")?.toLong() ?: throw BadRequestException("Не число")
        val userId = request.path("user_id")?.toLong() ?: throw BadRequestException("Не число")
        // TODO проверить что то пользователь отправил
        invitation.createInvitation(travelId, userId)
        return Response(Status.FOUND).header("Location", "/invitation/$travelId/create")
    }
}

class InvitationAccept(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val invitation: InvitationDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number").orEmpty().toLong()
        val currentUser = currentUserLens(request)
        invitation.invitationAccept(id, currentUser!!)
        return Response(Status.FOUND).header("Location", "/invitation/my")
    }
}

class InvitationCancel(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val invitation: InvitationDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = currentUserLens(request)
        val id = request.path("number").orEmpty().toLong()
        invitation.invitationCancel(id, user!!.id)
        return Response(Status.FOUND).header("Location", "/invitation/my")
    }
}
