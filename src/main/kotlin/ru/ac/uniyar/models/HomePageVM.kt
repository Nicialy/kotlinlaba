package ru.ac.uniyar.models

import ru.ac.uniyar.database.DBUserEntity

class HomePageVM(currentUser: DBUserEntity?) : AuthUserVM(currentUser)