package ru.ac.uniyar.models

import org.http4k.template.ViewModel
import ru.ac.uniyar.database.DBCrewEntity
import ru.ac.uniyar.database.DBInvitationEntity
import ru.ac.uniyar.database.DBUserEntity

class InvitationCreateVM(
    val crews: List<DBCrewEntity>,
    val users: List<DBUserEntity>,
    val invitations: List<DBInvitationEntity>
) : ViewModel