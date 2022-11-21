package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBInvitationEntity
import ru.ac.uniyar.utils.Pageable

class InvitationVM(
    val invitations: List<DBInvitationEntity>,
    val pageable: Pageable
) : ViewModel
