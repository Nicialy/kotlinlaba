package ru.ac.uniyar.quires

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.count
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import ru.ac.uniyar.database.DBTeamVisitorEntity
import ru.ac.uniyar.database.TeamVisitorTable
import ru.ac.uniyar.database.TravelTable
import ru.ac.uniyar.utils.CountShipException
import ru.ac.uniyar.utils.CrewNotReadyException
import ru.ac.uniyar.utils.NotFoundException
import ru.ac.uniyar.utils.UserFoundException

class TeamVisitorDB(private val database: Database) {
    fun getMyVisit(user_id: Long): List<DBTeamVisitorEntity> {
        return database.sequenceOf(TeamVisitorTable).filter { it.user_id eq user_id }.toList()
    }
    fun goVisit(travelId: Long, userId: Long) {
        database.useTransaction {
            val travel = database.sequenceOf(TravelTable).find { it.id eq travelId } ?: throw NotFoundException("Не найдено")
            val userHave = database.sequenceOf(TeamVisitorTable, withReferences = false).filter { it.travel_id eq travelId }.find { it.user_id eq userId }
            if (userHave != null) {
                throw UserFoundException("Пользователь найден")
            }
            val visitors = database.sequenceOf(TeamVisitorTable, withReferences = false).filter { it.travel_id eq travelId }.count()
            if (travel.ship_id.countPlace - visitors <= 0) {
                throw CountShipException("Мест нет")
            }
            if (travel.status != "Открыто для набора посетителей") {
                throw CrewNotReadyException("Рано для бронирования")
            }
            database.insertAndGenerateKey(TeamVisitorTable) {
                set(it.user_id, userId)
                set(it.travel_id, travelId)
            }
        }
    }
}
