package ru.ac.uniyar.models

import org.http4k.lens.WebForm
import ru.ac.uniyar.database.DBShipEntity
import ru.ac.uniyar.database.DBUserEntity

class TravelFormVM(currentUser: DBUserEntity?, val webForm: WebForm = WebForm(), val ships: List<DBShipEntity> = emptyList()) : AuthUserVM(currentUser)
