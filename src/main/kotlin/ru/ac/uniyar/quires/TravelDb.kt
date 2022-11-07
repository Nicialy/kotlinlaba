package ru.ac.uniyar.quires

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.map
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.entity.count
import org.ktorm.entity.drop
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.take
import org.ktorm.entity.toList
import ru.ac.uniyar.database.CrewTable
import ru.ac.uniyar.database.DBTravelEntity
import ru.ac.uniyar.database.DBUserEntity
import ru.ac.uniyar.database.TeamVisitorTable
import ru.ac.uniyar.database.TravelTable
import ru.ac.uniyar.database.UsersTable
import ru.ac.uniyar.utils.NotFoundException
import java.time.LocalDate

class TravelDb(private val database: Database) {
    fun createTravel(user_id: Long, name: String, shipId: Long, description: String, date_start: LocalDate, date_end: LocalDate): Long {
        database.useTransaction {
            val id = database.insertAndGenerateKey(TravelTable) {
                set(it.name, name)
                set(it.description, description)
                set(it.ship_id, shipId)
                set(it.date_start, date_start)
                set(it.date_end, date_end)
                set(it.status, "Идет набор команды")
            }

            database.insertAndGenerateKey(CrewTable) {
                set(it.travel_id, getMyLongValue(id))
                set(it.user_id, user_id)
            }
            return getMyLongValue(id)
        }
    }
    fun getAllTravel(page: Int, size: Int): MutableList<Pair<DBTravelEntity, List<DBUserEntity>>> {
        var travelsList = mutableListOf<Pair<DBTravelEntity, List<DBUserEntity>>>()
        database.useTransaction {
            val travels = database.sequenceOf(TravelTable).drop((page - 1) * size).take(size).toList()
            travels.forEach { travel: DBTravelEntity ->
                var user = database.from(CrewTable)
                    .leftJoin(UsersTable, on = UsersTable.id eq CrewTable.user_id).select()
                    .where { CrewTable.travel_id eq travel.id }.map { UsersTable.createEntity(it) }
                val visitors = database.sequenceOf(TeamVisitorTable, withReferences = false).filter { it.travel_id eq travel.id }.count()
                travel.ship_id.countPlace = travel.ship_id.countPlace - visitors
                travelsList.add(Pair(travel, user))
            }
            return travelsList
        }
    }
    fun getCountTravel(): Int {
        return database.sequenceOf(TravelTable).count()
    }
    fun fetch(id: Long): Pair<DBTravelEntity, List<DBUserEntity>> {
        database.useTransaction {
            val travel = database.sequenceOf(TravelTable).find { it.id eq id } ?: throw NotFoundException("Не найден")
            var users = database.from(CrewTable)
                .leftJoin(UsersTable, on = UsersTable.id eq CrewTable.user_id).select()
                .where { CrewTable.travel_id eq id }.map { UsersTable.createEntity(it) }
            return Pair(travel, users)
        }
    }
    fun travelDel(id: Long) {
        database.useTransaction {
            val travel = database.sequenceOf(TravelTable).find { it.id eq id } ?: throw NotFoundException("Не найден")
            travel.delete()
        }
    }
    private fun getMyLongValue(vararg any: Any): Long {
        return when (val tmp = any.first()) {
            is Number -> tmp.toLong()
            else -> throw Exception("not a number") // or do something else reasonable for your case
        }
    }
}
