package ru.ac.uniyar

import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.RequestContexts
import org.http4k.core.then
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.server.Undertow
import org.http4k.server.asServer
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.database.DbConfig
import ru.ac.uniyar.database.H2DatabaseManager
import ru.ac.uniyar.database.performMigrations
import ru.ac.uniyar.filters.ExceptionHandlingFilter
import ru.ac.uniyar.filters.FetchUserWithToken
import ru.ac.uniyar.filters.JwtTools
import ru.ac.uniyar.filters.authFilter
import ru.ac.uniyar.models.template.ContextAwarePebbleTemplates
import ru.ac.uniyar.models.template.ContextAwareViewRender
import ru.ac.uniyar.quires.BidDB
import ru.ac.uniyar.quires.CrewDb
import ru.ac.uniyar.quires.InvitationDb
import ru.ac.uniyar.quires.ShipDb
import ru.ac.uniyar.quires.TeamVisitorDB
import ru.ac.uniyar.quires.TravelDb
import ru.ac.uniyar.quires.UsersDb
import ru.ac.uniyar.utils.RolePermission
import kotlinx.coroutines.* // ktlint-disable no-wildcard-imports
import java.util.concurrent.TimeUnit

suspend fun main() {
    val contexts = RequestContexts()
    val currentUserLens = RequestContextKey.optional<DBUserEntity>(contexts)
    val currentUserPermission = RequestContextKey.required<RolePermission>(contexts)
    val jwtTools = JwtTools("dsaadsasd", "AndyWebService")

    val renderer = ContextAwarePebbleTemplates().HotReload("src/main/resources")
    val htmlView = ContextAwareViewRender(renderer, ContentType.TEXT_HTML)
    val h2databaseManager = H2DatabaseManager().initialize()

    val htmlViewWithContext = htmlView
        .associateContextLens("currentUser", currentUserLens)
        .associateContextLens("Permission", currentUserPermission)

    val database = DbConfig.getConnection()
    val fetchUserWithToken = FetchUserWithToken(database)
    val traveldb = TravelDb(database)
    val router = Router(
        currentUserPermission,
        currentUserLens,
        htmlViewWithContext,
        jwtTools,
        ShipDb(database),
        UsersDb(database),
        traveldb,
        BidDB(database),
        InvitationDb(database),
        CrewDb(database),
        TeamVisitorDB(database)
    )

    val printingApp: HttpHandler = ServerFilters.InitialiseRequestContext(contexts)
        .then(ExceptionHandlingFilter(htmlView)).then(
            authFilter(currentUserLens, currentUserPermission, fetchUserWithToken, jwtTools)
        ).then(
            router.invoke()
        )

    val webServer = printingApp.asServer(Undertow(port = 9000)).start()
    performMigrations()
    startInfiniteExecution(traveldb)
    println("Сервер запущен на  http://localhost:" + webServer.port())
    println("Доступ к бд возможен по  http://localhost:${H2DatabaseManager.WEB_PORT}")
    println("Траляля тополя")
    readlnOrNull()
    cancelTask()
    webServer.stop()
    h2databaseManager.stopServers()
}
private var job: Job? = null

suspend fun startInfiniteExecution(travelDb: TravelDb) {
    coroutineScope {
        job = launch() {
            while (true) {
                println("Check travels")
                travelDb.checkStart()
                travelDb.checkEnd()
                delay(TimeUnit.DAYS.toMillis(1))
            }
        }
    }
}

fun cancelTask() {
    job?.cancel()
}





