package ru.ac.uniyar.models

import org.http4k.core.Uri
import ru.ac.uniyar.database.DBUserEntity

class ShowErrorInfoVM(val uri: Uri, currentUser: DBUserEntity?, message: String?) : AuthUserVM(currentUser)
