package ru.ac.uniyar.models

import ru.ac.uniyar.database.DBShipEntity
import ru.ac.uniyar.database.DBUserEntity

class ShipGetVM(currentUser: DBUserEntity?, val ship: DBShipEntity) : AuthUserVM(currentUser)
