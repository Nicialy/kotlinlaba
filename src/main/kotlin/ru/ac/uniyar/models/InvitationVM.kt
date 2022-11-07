package ru.ac.uniyar.models

import ru.ac.uniyar.database.DBInvitationEntity
import ru.ac.uniyar.database.DBUserEntity

class InvitationVM(
    currentUser: DBUserEntity?,
    val invitations: List<DBInvitationEntity>
) : AuthUserVM(currentUser)
