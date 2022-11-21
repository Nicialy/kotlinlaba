package ru.ac.uniyar.quires

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.count
import org.ktorm.entity.drop
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.take
import org.ktorm.entity.toList
import ru.ac.uniyar.database.CrewTable
import ru.ac.uniyar.database.DBCrewEntity
import ru.ac.uniyar.database.DBInvitationEntity
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.database.InvitationTable
import ru.ac.uniyar.utils.ForbiddenException
import ru.ac.uniyar.utils.NotFoundException

class InvitationDb(private val database: Database) {
    fun createInvitation(travel_id: Long, user_id: Long): Any {
        val id = database.insertAndGenerateKey(InvitationTable) {
            set(it.travel_id, travel_id)
            set(it.user_id, user_id)
            set(it.status, "Не ответил")
        }
        return id
    }
    fun travelInvitation(travel_id: Long): List<DBInvitationEntity> {
        return database.sequenceOf(InvitationTable).filter { it.travel_id eq travel_id }.toList()
    }
    fun myInvitation(user_id: Long, page: Int, size: Int, filter: String): List<DBInvitationEntity> {
        return database.sequenceOf(InvitationTable).filter { it.user_id eq user_id }.filter { it.status eq filter }.drop((page - 1) * size).take(size).toList()
    }
    fun invitationDel(id: Long) {
        database.useTransaction {
            val travel = database.sequenceOf(InvitationTable).find { it.id eq id } ?: throw NotFoundException("Не найден")
            travel.delete()
        }
    }
    fun invitationAccept(id: Long, user: DBUserEntity) {
        database.useTransaction {
            val invitation = database.sequenceOf(InvitationTable).find { it.id eq id } ?: throw NotFoundException("Не найдено")
            if (invitation.user_id.id != user.id) {
                throw ForbiddenException("Нет доступа")
            }
            invitation.status = "Принял"

            database.insertAndGenerateKey(CrewTable) {
                set(it.travel_id, invitation.travel_id.id)
                set(it.user_id, user.id)
            }
            val crews = database.sequenceOf(CrewTable).filter { it.travel_id eq invitation.travel_id.id }.toList()
            if ((crews.size >= 5) and (invitation.travel_id.status != "Открыто для набора посетителей")) {
                var countMatrosov = 0
                var capitan = false
                var bochman = false
                var cock = false
                crews.forEach { crew: DBCrewEntity ->
                    when (crew.user_id.role) {
                        "Администратор" -> capitan = true
                        "Капитан" -> capitan = true
                        "Повар" -> cock = true
                        "Боцман" -> bochman = true
                        "Матрос" -> countMatrosov += 1
                    }
                }
                if ((countMatrosov >= 2) and capitan and bochman and cock) {
                    invitation.travel_id.status = "Открыто для набора посетителей"
                    invitation.travel_id.flushChanges()
                }
            }
            invitation.flushChanges()
        }
    }
    fun invitationCancel(id: Long, userId: Long) {
        database.useTransaction {
            val invitation = database.sequenceOf(InvitationTable).filter { it.user_id eq userId }.find { it.id eq id } ?: throw NotFoundException("Не найдено")
            invitation.status = "Отказал"
            invitation.flushChanges()
        }
    }
    fun getMyCountInvitation(user: DBUserEntity, status: String): Int {
        return database.sequenceOf(InvitationTable).filter { it.user_id eq user.id }.filter { it.status eq status }.count()
    }
}
