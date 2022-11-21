package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBBidEntity
import ru.ac.uniyar.utils.Pageable

class BidVM(val bids: List<DBBidEntity>, val pageable: Pageable) : ViewModel
