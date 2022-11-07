package ru.ac.uniyar.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiLens
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.models.ShowErrorInfoVM
import ru.ac.uniyar.utils.BadRequestException
import ru.ac.uniyar.utils.CountShipException
import ru.ac.uniyar.utils.CrewNotReadyException
import ru.ac.uniyar.utils.ForbiddenException
import ru.ac.uniyar.utils.NotFoundException
import ru.ac.uniyar.utils.UserFoundException
import ru.ac.uniyar.utils.UserNotException

class ExceptionHandlingFilter(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>
) : Filter {
    override fun invoke(next: HttpHandler) = ExceptionHandler(currentUserLens, htmlView, next)
}

class ExceptionHandler(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    val next: HttpHandler
) : HttpHandler {
    override fun invoke(request: Request): Response {
        return try {
            next(request)
        } catch (e: ForbiddenException) {
            val currentUser = currentUserLens(request)
            return Response(Status.FORBIDDEN).with(htmlView of ShowErrorInfoVM(request.uri, currentUser, e.message))
        } catch (e: NotFoundException) {
            val currentUser = currentUserLens(request)
            return Response(Status.NOT_FOUND).with(htmlView of ShowErrorInfoVM(request.uri, currentUser, e.message))
        } catch (e: BadRequestException) {
            val currentUser = currentUserLens(request)
            return Response(Status.NOT_FOUND).with(htmlView of ShowErrorInfoVM(request.uri, currentUser, e.message))
        } catch (e: CountShipException) {
            val currentUser = currentUserLens(request)
            return Response(Status.NOT_FOUND).with(htmlView of ShowErrorInfoVM(request.uri, currentUser, e.message))
        } catch (e: UserFoundException) {
            val currentUser = currentUserLens(request)
            return Response(Status.NOT_FOUND).with(htmlView of ShowErrorInfoVM(request.uri, currentUser, e.message))
        } catch (e: UserNotException) {
            val currentUser = currentUserLens(request)
            return Response(Status.NOT_FOUND).with(htmlView of ShowErrorInfoVM(request.uri, currentUser, e.message))
        } catch (e: CrewNotReadyException) {
            val currentUser = currentUserLens(request)
            return Response(Status.NOT_FOUND).with(htmlView of ShowErrorInfoVM(request.uri, currentUser, e.message))
        } catch (e: Exception) {
            val currentUser = currentUserLens(request)
            Response(Status.INTERNAL_SERVER_ERROR).with(htmlView of ShowErrorInfoVM(request.uri, currentUser, e.message))
        }
    }
}
