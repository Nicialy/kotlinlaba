package ru.ac.uniyar.utils

import org.http4k.core.Status

open class CommonException(message: String, val code: Status) : Exception(message)
class PasswordNotException(message: String) : CommonException(message, code = Status.BAD_REQUEST)
class ForbiddenException(message: String) : CommonException(message, code = Status.FORBIDDEN)
class NotAuthException(message: String) : CommonException(message, code = Status.UNAUTHORIZED)
class NotFoundException(message: String) : CommonException(message, code = Status.NOT_FOUND)
class NotFoundInvitaionException(message: String) : CommonException(message, code = Status.NOT_FOUND)
class BadRequestException(message: String) : CommonException(message, code = Status.BAD_REQUEST)
class CountShipException(message: String) : CommonException(message, code = Status.NOT_FOUND)
class UserFoundException(message: String) : CommonException(message, code = Status.NOT_FOUND)
class UserNotException(message: String) : CommonException(message, code = Status.NOT_FOUND)
class CrewNotReadyException(message: String) : CommonException(message, code = Status.NOT_FOUND)
