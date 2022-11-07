package ru.ac.uniyar.models

import ru.ac.uniyar.database.DBCrewEntity
import ru.ac.uniyar.database.DBInvitationEntity
import ru.ac.uniyar.database.DBUserEntity

class InvitationCreateVM(
    currentUser: DBUserEntity?,
    val crews: List<DBCrewEntity>,
    val users: List<DBUserEntity>,
    val invitations: List<DBInvitationEntity>
) : AuthUserVM(currentUser)