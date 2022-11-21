package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBTravelEntity
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.utils.Pageable

class TravelsFormVM(
    val travels: List<Pair<DBTravelEntity, List<DBUserEntity>>>,
    val pageable: Pageable
) : ViewModel
