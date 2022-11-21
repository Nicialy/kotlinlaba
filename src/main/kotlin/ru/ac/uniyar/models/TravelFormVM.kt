package ru.ac.uniyar.models

import org.http4k.lens.WebForm
import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBShipEntity

class TravelFormVM(val webForm: WebForm = WebForm(), val ships: List<DBShipEntity> = emptyList()) : ViewModel
