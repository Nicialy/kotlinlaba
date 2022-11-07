package ru.ac.uniyar.quires

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.filter
import org.ktorm.entity.filterNot
import org.ktorm.entity.find
import org.ktorm.entity.first
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import ru.ac.uniyar.database.CrewTable
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.database.InvitationTable
import ru.ac.uniyar.database.UsersTable
import ru.ac.uniyar.utils.PasswordNotException
import ru.ac.uniyar.utils.UserNotException
import ru.ac.uniyar.utils.hashPassword

class UsersDb(
    private val database: Database
) {
    fun checkPassword(nickname: String, password: String) {
        database.useTransaction {
            val user =
                database.sequenceOf(UsersTable).find { it.nickname eq nickname } ?: throw UserNotException("Нет такого")
            if (user.password != hashPassword(password)) {
                throw PasswordNotException("Пароли не совпадают")
            }
        }
    }
    fun createUser(nickname: String, password: String, name: String, surname: String, middle_name: String) {
        database.insertAndGenerateKey(UsersTable) {
            set(it.name, name)
            set(it.nickname, nickname)
            set(it.surname, surname)
            set(it.middle_name, middle_name)
            set(it.password, hashPassword(password))
            set(it.role, "Посетитель")
        }
    }
    fun fetch(id: Long): DBUserEntity {
        return database.sequenceOf(UsersTable).filter { it.id eq id }.first()
    }
    fun fetchUsersWithRole(travel_id: Long): List<DBUserEntity> {
        var usersList = mutableListOf<DBUserEntity>()
        database.useTransaction {
            val users = database.sequenceOf(UsersTable).filterNot { it.role eq "Посетитель" }.toList()
            users.forEach { user: DBUserEntity ->
                val crew = database.sequenceOf(CrewTable, withReferences = false).filter { it.travel_id eq travel_id }.find { it.user_id eq user.id }
                val invite = database.sequenceOf(InvitationTable, withReferences = false)
                    .filter { it.travel_id eq travel_id }
                    .filter { it.status eq "Не ответил" }.find { it.user_id eq user.id }
                if ((crew == null) and (invite == null)) {
                    usersList.add(user)
                }
            }
        }
        return usersList
    }
}
