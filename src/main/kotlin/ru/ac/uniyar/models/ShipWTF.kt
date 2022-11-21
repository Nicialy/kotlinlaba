package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBShipEntity
import ru.ac.uniyar.utils.Pageable

class ShipWTF(val myshipsList: List<DBShipEntity>, val pageable: Pageable) : ViewModel
