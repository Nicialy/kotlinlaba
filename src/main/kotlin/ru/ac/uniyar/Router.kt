package ru.ac.uniyar

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.lens.BiDiLens
import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.endpoints.BidRoute
import ru.ac.uniyar.endpoints.InvitationRoute
import ru.ac.uniyar.endpoints.LogOutUser
import ru.ac.uniyar.endpoints.PhotoGet
import ru.ac.uniyar.endpoints.crewRoute
import ru.ac.uniyar.endpoints.loginRoute
import ru.ac.uniyar.endpoints.registationRoute
import ru.ac.uniyar.endpoints.rootHandler
import ru.ac.uniyar.endpoints.shipsRoute
import ru.ac.uniyar.endpoints.teamVisitorRoute
import ru.ac.uniyar.endpoints.travelRoute
import ru.ac.uniyar.filters.JwtTools
import ru.ac.uniyar.filters.roleFilter
import ru.ac.uniyar.models.template.ContextAwareViewRender
import ru.ac.uniyar.quires.BidDB
import ru.ac.uniyar.quires.CrewDb
import ru.ac.uniyar.quires.InvitationDb
import ru.ac.uniyar.quires.ShipDb
import ru.ac.uniyar.quires.TeamVisitorDB
import ru.ac.uniyar.quires.TravelDb
import ru.ac.uniyar.quires.UsersDb
import ru.ac.uniyar.utils.RolePermission

class Router(
    private val currentRolePermissionLens: BiDiLens<Request, RolePermission>,
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: ContextAwareViewRender,
    private val jwtTools: JwtTools,
    private val ship: ShipDb,
    private val user: UsersDb,
    private val travel: TravelDb,
    private val bid: BidDB,
    private val invitation: InvitationDb,
    private val crew: CrewDb,
    private val teamVis: TeamVisitorDB
) {
    operator fun invoke(): RoutingHttpHandler = routes(
        "/" bind Method.GET to rootHandler(htmlView),
        "/registration" bind roleFilter(currentRolePermissionLens, "registration").then(registationRoute(htmlView, user)),
        "/ship" bind shipsRoute(htmlView, currentRolePermissionLens, ship),
        "/login" bind roleFilter(currentRolePermissionLens, "login").then(loginRoute(htmlView, jwtTools, user)),
        "/logout" bind Method.GET to roleFilter(currentRolePermissionLens, "logout").then(LogOutUser()),
        "/travel" bind travelRoute(currentUserLens, currentRolePermissionLens, htmlView, travel, ship),
        "/bid" bind BidRoute(currentUserLens, currentRolePermissionLens, htmlView, bid),
        "/invitation" bind roleFilter(currentRolePermissionLens, "invitation").then(InvitationRoute(currentUserLens, htmlView, crew, invitation, user)),
        "/crew" bind crewRoute(currentUserLens, crew),
        "/visit" bind roleFilter(currentRolePermissionLens, "visit").then(teamVisitorRoute(currentUserLens, teamVis, currentRolePermissionLens)),

        "/photo/{name}" bind Method.GET to PhotoGet(),
        static(ResourceLoader.Classpath("ru/ac/uniyar/public/"))
    )
}
