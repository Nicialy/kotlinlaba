package ru.ac.uniyar.models

import ru.ac.uniyar.database.DBShipEntity
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.utils.Pageable

class ShipWTF(currentUser: DBUserEntity?, val myshipsList: List<DBShipEntity>,val pageable: Pageable) : AuthUserVM(currentUser)