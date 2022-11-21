package ru.ac.uniyar.endpoints

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.FormField
import org.http4k.lens.Invalid
import org.http4k.lens.Validator
import org.http4k.lens.nonEmptyString
import org.http4k.lens.webForm
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import ru.ac.uniyar.models.RegistrationVM
import ru.ac.uniyar.models.template.ContextAwareViewRender
import ru.ac.uniyar.quires.UsersDb
import ru.ac.uniyar.utils.PasswordNotException

fun registationRoute(
    htmlView: ContextAwareViewRender,
    user: UsersDb
): RoutingHttpHandler = routes(
    "/" bind Method.GET to registrationGet(htmlView),
    "/" bind Method.POST to AddNewUser(htmlView, user)
)

class AddNewUser(
    private val htmlView: ContextAwareViewRender,
    private val user: UsersDb
) : HttpHandler {
    companion object {
        val nicknameFormLens = FormField.nonEmptyString().required("nickname")
        val nameFormLens = FormField.nonEmptyString().required("name")
        val surnameFormLens = FormField.nonEmptyString().required("surname")
        val middlenameFormLens = FormField.nonEmptyString().required("middlename")
        val passwordOneFormLens = FormField.nonEmptyString().required("passwordOne")
        val passwordTwoFormLens = FormField.nonEmptyString().required("passwordTwo")
        val registFormLens = Body.webForm(
            Validator.Feedback,
            nicknameFormLens,
            nameFormLens,
            surnameFormLens,
            middlenameFormLens,
            passwordOneFormLens,
            passwordTwoFormLens
        ).toLens()
    }

    override fun invoke(request: Request): Response {
        var webForm = registFormLens(request)
        try {
            if (webForm.errors.isEmpty()) {
                if (passwordOneFormLens(webForm) != passwordTwoFormLens(webForm)) {
                    throw PasswordNotException("Пароли не равны")
                }
                user.createUser(
                    nicknameFormLens(webForm),
                    passwordOneFormLens(webForm),
                    nameFormLens(webForm),
                    surnameFormLens(webForm),
                    middlenameFormLens(webForm)
                )
                return Response(Status.FOUND).header("Location", "/login")
            }
        } catch (_: PasswordNotException) {
            val newErrors = webForm.errors + Invalid(passwordOneFormLens.meta.copy(description = "password bad"))
            webForm = webForm.copy(errors = newErrors)
        }
        return Response(OK).with(htmlView(request) of RegistrationVM(webForm))
    }
}

fun registrationGet(htmlView: ContextAwareViewRender): HttpHandler = {
    Response(OK).with(htmlView(it) of RegistrationVM())
}
