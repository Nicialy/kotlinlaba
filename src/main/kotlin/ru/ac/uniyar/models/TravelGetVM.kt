package ru.ac.uniyar.models


import ru.ac.uniyar.database.DBTravelEntity
import ru.ac.uniyar.database.DBUserEntity

class TravelGetVM(currentUser: DBUserEntity?, val travel: Pair<DBTravelEntity, List<DBUserEntity>>) : AuthUserVM(currentUser)