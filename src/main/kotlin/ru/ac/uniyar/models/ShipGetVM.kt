package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBShipEntity

class ShipGetVM(val ship: DBShipEntity) : ViewModel
