package ru.ac.uniyar.endpoints

import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.filters.roleBiggerFilter
import ru.ac.uniyar.models.InvitationCreateVM
import ru.ac.uniyar.models.InvitationVM
import ru.ac.uniyar.quires.CrewDb
import ru.ac.uniyar.quires.InvitationDb
import ru.ac.uniyar.quires.UsersDb
import ru.ac.uniyar.utils.BadRequestException

fun InvitationRoute(
    currentUserLens: BiDiLens<Request, DBUserEntity?>,
    htmlView: BiDiBodyLens<ViewModel>,
    crew: CrewDb,
    invitation: InvitationDb,
    user: UsersDb
): RoutingHttpHandler =
    routes(
        "{number}/create" bind Method.GET to roleBiggerFilter(currentUserLens, 5).then(InvitationCreateGet(currentUserLens, htmlView, crew, invitation, user)),
        "{number}/create/{user_id}" bind Method.POST to roleBiggerFilter(currentUserLens, 5).then(InvitationCreatePost(currentUserLens, htmlView, crew, invitation)),
        "/accept/{number}" bind Method.POST to roleBiggerFilter(currentUserLens, 2).then(InvitationAccept(currentUserLens, invitation)),
        "/cancel/{number}" bind Method.POST to roleBiggerFilter(currentUserLens, 2).then(InvitationCancel(currentUserLens, htmlView, crew, invitation)),
        "/delete/{number}" bind Method.POST to roleBiggerFilter(currentUserLens, 2).then(InvitationDelete(currentUserLens, htmlView, crew, invitation)),
        "/my" bind Method.GET to roleBiggerFilter(currentUserLens, 2).then(InvitationMy(currentUserLens, htmlView, invitation))
    )

class InvitationMy(
    private val currentUserLens: BiDiLens<Request, DBUserEntity??>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val invitation: InvitationDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val user = currentUserLens(request)
        val invitationUser = invitation.myInvitation(user!!.id)
        return Response(Status.OK).with(htmlView of InvitationVM(user, invitationUser))
    }
}

class InvitationCreateGet(
    private val currentUserLens: BiDiLens<Request, DBUserEntity??>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val crew: CrewDb,
    private val invitation: InvitationDb,
    private val userCanInvite: UsersDb

) : HttpHandler {
    override fun invoke(request: Request): Response {
        val id = request.path("number").orEmpty().toLong()
        val user = currentUserLens(request)
        val crewUsers = crew.getCrewTravel(id)
        val invitationUsers = invitation.travelInvitation(id)
        val usersCan = userCanInvite.fetchUsersWithRole(id)
        return Response(Status.OK).with(htmlView of InvitationCreateVM(user, crewUsers, usersCan, invitationUsers))
    }
}

class InvitationCreatePost(
    private val currentUserLens: BiDiLens<Request, DBUserEntity??>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val crew: CrewDb,
    private val invitation: InvitationDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val travelId = request.path("number")?.toLong() ?: throw BadRequestException("Не число")
        val userId = request.path("user_id")?.toLong() ?: throw BadRequestException("Не число")
        // TODO проверить что то пользователь отправил
        invitation.createInvitation(travelId, userId)
        return Response(Status.NO_CONTENT).header("Location", request.uri.toString())
    }
}

class InvitationAccept(
    private val currentUserLens: BiDiLens<Request, DBUserEntity??>,
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
    private val currentUserLens: BiDiLens<Request, DBUserEntity??>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val crew: CrewDb,
    private val invitation: InvitationDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        // TODO проверка что это тот юзер
        val id = request.path("number").orEmpty().toLong()
        // bid.cancelBid(id)
        invitation.invitationCancel(id)
        return Response(Status.FOUND).header("Location", "/invitation/my")
    }
}
class InvitationDelete(
    private val currentUserLens: BiDiLens<Request, DBUserEntity??>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    private val crew: CrewDb,
    private val invitation: InvitationDb
) : HttpHandler {
    override fun invoke(request: Request): Response {
        val currentUser = currentUserLens(request)
        val id = request.path("number").orEmpty().toLong()
        // currentUser!!.id?.let { bid.delMyBid(it, id) } ?: NotAuthException("Не авторизован")
        return Response(Status.FOUND).header("Location", "/invitation/my")
    }
}
