package ru.ac.uniyar.quires

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.toList
import ru.ac.uniyar.database.CrewTable
import ru.ac.uniyar.database.DBCrewEntity
import ru.ac.uniyar.database.DBTravelEntity
import ru.ac.uniyar.utils.ForbiddenException
import ru.ac.uniyar.utils.NotFoundException

class CrewDb(private val database: Database) {

    fun getCrewTravel(travel_id: Long): List<DBCrewEntity> {
        return database.sequenceOf(CrewTable).filter { it.travel_id eq travel_id }.toList()
    }
    fun crewDelete(crew_id: Long, user_id: Long): Long {
        database.useTransaction {
            val crew = database.sequenceOf(CrewTable).find { it.id eq crew_id } ?: throw NotFoundException("Не найден")
            database.sequenceOf(CrewTable).filter { it.travel_id eq crew.travel_id.id }.find { it.user_id eq user_id } ?: throw ForbiddenException("Нет доступа")
            val travelId = crew.travel_id.id
            crew.delete()
            return travelId
        }
    }
}
