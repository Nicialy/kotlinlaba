package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBTravelEntity
import ru.ac.uniyar.database.DBUserEntity

class TravelGetVM(val travel: Pair<DBTravelEntity, List<DBUserEntity>>) : ViewModel