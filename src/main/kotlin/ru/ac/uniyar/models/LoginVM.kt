package ru.ac.uniyar.models

import org.http4k.lens.WebForm
import ru.ac.uniyar.database.DBUserEntity

class LoginVM(currentUser: DBUserEntity?, val form: WebForm = WebForm()) : AuthUserVM(currentUser)