package ru.ac.uniyar.filters

import org.http4k.core.Filter
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.BiDiBodyLens
import org.http4k.lens.BiDiLens
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.utils.CountShipException
import ru.ac.uniyar.utils.UserFoundException


class TravelHandlingFilter(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>
) : Filter {
    override fun invoke(next: HttpHandler) = TravelExceptionHandler(currentUserLens, htmlView, next)
}


class TravelExceptionHandler(
    private val currentUserLens: BiDiLens<Request, DBUserEntity?>,
    private val htmlView: BiDiBodyLens<ViewModel>,
    val next: HttpHandler
) : HttpHandler {
    override fun invoke(request: Request): Response {
       return try {
            next(request)
        }catch (e: UserFoundException)
        {
            return Response(Status.OK)
            //return TravelsGet(currentUserLens)
        }
        catch (e: CountShipException)
        {
            return Response(Status.OK)
            //return Response(Status.OK).with(htmlView of TravelsFormVM(currentUser, travels, pagination))
        }
    }
}