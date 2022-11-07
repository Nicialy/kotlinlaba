package ru.ac.uniyar.models

import org.http4k.lens.MultipartForm
import ru.ac.uniyar.database.DBUserEntity

class ShipGetCreateVM(currentUser: DBUserEntity?, val form: MultipartForm = MultipartForm()) : AuthUserVM(currentUser)
