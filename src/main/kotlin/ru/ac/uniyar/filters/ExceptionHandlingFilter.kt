package ru.ac.uniyar.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import ru.ac.uniyar.models.ShowErrorInfoVM
import ru.ac.uniyar.models.template.ContextAwareViewRender
import ru.ac.uniyar.utils.CommonException

class ExceptionHandlingFilter(
    private val htmlView: ContextAwareViewRender
) : Filter {
    override fun invoke(next: HttpHandler) = ExceptionHandler(htmlView, next)
}

class ExceptionHandler(
    private val htmlView: ContextAwareViewRender,
    val next: HttpHandler
) : HttpHandler {
    override fun invoke(request: Request): Response {
        return try {
            next(request)
        } catch (e: CommonException) {
            return errorResponse(htmlView, request, e)
        } catch (e: Exception) {
            Response(Status.INTERNAL_SERVER_ERROR).with(htmlView(request) of ShowErrorInfoVM(request.uri,  e.message))
        }
    }
}

fun errorResponse(htmlView: ContextAwareViewRender, request: Request, e: CommonException): Response {
    return Response(e.code).with(htmlView(request) of ShowErrorInfoVM(request.uri, e.message))
}
