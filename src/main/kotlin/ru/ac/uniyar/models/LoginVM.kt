package ru.ac.uniyar.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBUserEntity

class LoginVM(val form: WebForm = WebForm()) : ViewModel