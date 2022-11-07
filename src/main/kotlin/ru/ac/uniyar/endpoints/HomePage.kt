package ru.ac.uniyar.endpoints

import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiLens
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.models.HomePageVM

fun rootHandler(currentUserLens: BiDiLens<Request, DBUserEntity?>, htmlView: BiDiBodyLens<ViewModel>): HttpHandler = {
    val currentUser = currentUserLens(it)
    Response(Status.OK).with(htmlView of HomePageVM(currentUser))
}
