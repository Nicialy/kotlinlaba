package ru.ac.uniyar

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.then
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiLens
import org.http4k.routing.ResourceLoader
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.routing.static
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.endpoints.AddNewUser
import ru.ac.uniyar.endpoints.BidRoute
import ru.ac.uniyar.endpoints.InvitationRoute
import ru.ac.uniyar.endpoints.LogOutUser
import ru.ac.uniyar.endpoints.LoginUser
import ru.ac.uniyar.endpoints.RegistrationGet
import ru.ac.uniyar.endpoints.crewRoute
import ru.ac.uniyar.endpoints.loginHandlerGet
import ru.ac.uniyar.endpoints.rootHandler
import ru.ac.uniyar.endpoints.shipsRoute
import ru.ac.uniyar.endpoints.teamVisitorRoute
import ru.ac.uniyar.endpoints.travelRoute
import ru.ac.uniyar.filters.JwtTools
import ru.ac.uniyar.filters.oneRoleFilter
import ru.ac.uniyar.filters.roleBiggerFilter
import ru.ac.uniyar.quires.BidDB
import ru.ac.uniyar.quires.CrewDb
import ru.ac.uniyar.quires.InvitationDb
import ru.ac.uniyar.quires.ShipDb
import ru.ac.uniyar.quires.TeamVisitorDB
import ru.ac.uniyar.quires.TravelDb
import ru.ac.uniyar.quires.UsersDb

class Router(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
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
        "/" bind Method.GET to roleBiggerFilter(currentUserLens, 0).then(rootHandler(currentUserLens, htmlView)),
        "/registration" bind Method.GET to oneRoleFilter(currentUserLens, 0).then(RegistrationGet(currentUserLens, htmlView)),
        "/registration" bind Method.POST to oneRoleFilter(currentUserLens, 0).then(AddNewUser(currentUserLens, htmlView, user)),
        "/ship" bind shipsRoute(currentUserLens, htmlView, ship),
        "/login" bind Method.GET to oneRoleFilter(currentUserLens, 0).then(loginHandlerGet(currentUserLens, htmlView)),
        "/login" bind Method.POST to oneRoleFilter(currentUserLens, 0).then(LoginUser(currentUserLens, htmlView, jwtTools, user)),
        "/logout" bind Method.GET to roleBiggerFilter(currentUserLens, 1).then(LogOutUser()),
        "/travel" bind travelRoute(currentUserLens, htmlView, travel, ship),
        "/bid" bind BidRoute(currentUserLens, htmlView, bid),
        "/invitation" bind InvitationRoute(currentUserLens, htmlView, crew, invitation, user),
        "/crew" bind crewRoute(currentUserLens, crew),
        "/visit" bind teamVisitorRoute(currentUserLens, teamVis),
        static(ResourceLoader.Classpath("ru/ac/uniyar/models/public/"))
    )
}
