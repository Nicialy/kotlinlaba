package ru.ac.uniyar.endpoints

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.cookie.Cookie
import org.http4k.core.cookie.SameSite
import org.http4k.core.cookie.cookie
import org.http4k.core.cookie.invalidateCookie
import org.http4k.core.with
import org.http4k.lens.FormField
import org.http4k.lens.Invalid
import org.http4k.lens.Validator
import org.http4k.lens.nonEmptyString
import org.http4k.lens.webForm
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.filters.JwtTools
import ru.ac.uniyar.models.LoginVM
import ru.ac.uniyar.models.template.ContextAwareViewRender
import ru.ac.uniyar.quires.UsersDb
import ru.ac.uniyar.utils.PasswordNotException
import ru.ac.uniyar.utils.UserNotException

fun loginRoute(
    htmlView: ContextAwareViewRender,
    jwtTools: JwtTools,
    user: UsersDb
): RoutingHttpHandler = routes(
    "/" bind Method.GET to LoginHandlerGet(htmlView),
    "/" bind Method.POST to LoginUser(htmlView, jwtTools, user)
)
class LoginUser(
    private val htmlView: ContextAwareViewRender,
    private val jwtTools: JwtTools,
    private val user: UsersDb
) : HttpHandler {
    companion object {
        private val nicknameFormLens = FormField.nonEmptyString().required("nickname")
        private val passwordFormLens = FormField.nonEmptyString().required("password")
        private val loginFormLens = Body.webForm(
            Validator.Feedback,
            nicknameFormLens,
            passwordFormLens
        ).toLens()
    }

    override fun invoke(request: Request): Response {
        var webForm = loginFormLens(request)
        try {
            if (webForm.errors.isEmpty()) {
                user.checkPassword(nicknameFormLens(webForm), passwordFormLens(webForm))
                val token = jwtTools.create(nicknameFormLens(webForm)) ?: return Response(Status.INTERNAL_SERVER_ERROR)
                val authCookie = Cookie("token", token, httpOnly = true, sameSite = SameSite.Strict)
                return Response(FOUND)
                    .header("Location", "/")
                    .cookie(authCookie)
            }
        } catch (_: PasswordNotException) {
            val newErrors = webForm.errors + Invalid(passwordFormLens.meta.copy(description = "password bad"))
            webForm = webForm.copy(errors = newErrors)
        } catch (_: UserNotException) {
            val newErrors = webForm.errors + Invalid(passwordFormLens.meta.copy(description = "password bad"))
            webForm = webForm.copy(errors = newErrors)
        }
        return Response(OK).with(htmlView(request) of LoginVM(webForm))
    }
}

class LoginHandlerGet(
    private val htmlView: ContextAwareViewRender
) : HttpHandler {
    override fun invoke(request: Request): Response {
        return Response(OK).with(htmlView(request) of LoginVM())
    }
}
class LogOutUser : HttpHandler {
    override fun invoke(request: Request): Response {
        return Response(FOUND)
            .header("Location", "/")
            .invalidateCookie("token")
    }
}
