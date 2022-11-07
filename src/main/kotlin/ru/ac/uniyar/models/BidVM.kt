package ru.ac.uniyar.models

import ru.ac.uniyar.database.DBBidEntity
import ru.ac.uniyar.database.DBUserEntity

class BidVM(currentUser: DBUserEntity?, val uri: String, val bids: List<DBBidEntity> = listOf()) : AuthUserVM(currentUser)
