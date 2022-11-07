package ru.ac.uniyar.models

import ru.ac.uniyar.database.DBTravelEntity
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.utils.Pageable

class TravelsFormVM(
    currentUser: DBUserEntity?,
    val travels: MutableList<Pair<DBTravelEntity, List<DBUserEntity>>>,
    val pageable: Pageable
) : AuthUserVM(currentUser)
