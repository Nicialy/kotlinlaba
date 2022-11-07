package ru.ac.uniyar.utils


open class CommonException(message: String) : Exception(message)
class PasswordNotException(message: String) : CommonException(message)
class ForbiddenException(message: String) : CommonException(message)
class NotAuthException(message: String) : CommonException(message)
class NotFoundException(message: String) : CommonException(message)
class NotFoundInvitaionException(message: String) : CommonException(message)
class BadRequestException(message: String) : CommonException(message)
class CountShipException(message: String) : CommonException(message)
class UserFoundException(message: String) : CommonException(message)
class UserNotException(message: String) : CommonException(message)
class CrewNotReadyException(message: String) : CommonException(message)