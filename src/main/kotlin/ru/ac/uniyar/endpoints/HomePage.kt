package ru.ac.uniyar.endpoints

import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import ru.ac.uniyar.models.HomePageVM
import ru.ac.uniyar.models.template.ContextAwareViewRender

fun rootHandler(
    htmlView: ContextAwareViewRender
): HttpHandler = {
    Response(Status.OK).with(htmlView(it) of HomePageVM())
}
