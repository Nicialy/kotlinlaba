package ru.ac.uniyar

import org.http4k.core.Body
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.server.Undertow
import org.http4k.server.asServer
import org.http4k.template.PebbleTemplates
import org.http4k.template.viewModel
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.database.DbConfig
import ru.ac.uniyar.database.H2DatabaseManager
import ru.ac.uniyar.database.performMigrations
import ru.ac.uniyar.filters.ExceptionHandlingFilter
import ru.ac.uniyar.filters.FetchUserWithToken
import ru.ac.uniyar.filters.JwtTools
import ru.ac.uniyar.filters.authFilter
import ru.ac.uniyar.quires.BidDB
import ru.ac.uniyar.quires.CrewDb
import ru.ac.uniyar.quires.InvitationDb
import ru.ac.uniyar.quires.ShipDb
import ru.ac.uniyar.quires.TeamVisitorDB
import ru.ac.uniyar.quires.TravelDb
import ru.ac.uniyar.quires.UsersDb


fun main() {
    val contexts = RequestContexts()
    val currentUserLens = RequestContextKey.optional<DBUserEntity>(contexts)

    val jwtTools = JwtTools("dsaadsasd", "AndyWebService")
    val renderer = PebbleTemplates().HotReload("src/main/resources")
    val htmlView = Body.viewModel(renderer, ContentType.TEXT_HTML).toLens()

    val h2databaseManager = H2DatabaseManager().initialize()

    val database = DbConfig.getConnection()
    val fetchUserWithToken = FetchUserWithToken(database)
    val router = Router(
        currentUserLens,
        htmlView,
        jwtTools,
        ShipDb(database),
        UsersDb(database),
        TravelDb(database),
        BidDB(database),
        InvitationDb(database),
        CrewDb(database),
        TeamVisitorDB(database)
    )
    val printingApp: HttpHandler = ServerFilters.InitialiseRequestContext(contexts)
        .then(ExceptionHandlingFilter(currentUserLens, htmlView)).then(
            authFilter(currentUserLens, fetchUserWithToken, jwtTools)
        ).then(
            DebuggingFilters.PrintRequest().then(router.invoke())
        )
    val webServer = printingApp.asServer(Undertow(port = 9000)).start()

    performMigrations()
    println("Сервер запущен на  http://localhost:" + webServer.port())
    println("Доступ к бд возможен по  http://localhost:${H2DatabaseManager.WEB_PORT}")
    println("Траляля тополя")
    readlnOrNull()
    webServer.stop()
    h2databaseManager.stopServers()
}
