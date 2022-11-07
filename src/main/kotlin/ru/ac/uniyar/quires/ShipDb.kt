package ru.ac.uniyar.quires

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.count
import org.ktorm.entity.drop
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.take
import org.ktorm.entity.toList
import ru.ac.uniyar.database.DBShipEntity
import ru.ac.uniyar.database.ShipTable
import ru.ac.uniyar.utils.NotFoundException

class ShipDb(private val database: Database) {
    fun createShip(name: String, description: String, url: String, count: Int): Any {
        val id = database.insertAndGenerateKey(ShipTable) {
            set(it.name, name)
            set(it.description, description)
            set(it.url, url)
            set(it.countPlace, count)
            set(it.status, "Свободен")
        }
        return id
    }

    fun fetch(id: Long): DBShipEntity {
        return database.sequenceOf(ShipTable).find { it.id eq id } ?: throw NotFoundException("Не найден")
    }
    fun getAllShips(page: Int, size: Int): List<DBShipEntity> {
        return database.sequenceOf(ShipTable).drop((page - 1) * size).take(size).toList()
    }
    fun getAllShips(): List<DBShipEntity> {
        return database.sequenceOf(ShipTable).toList()
    }
    fun shipDel(id: Long) {
        database.useTransaction {
            val ship = database.sequenceOf(ShipTable).find { it.id eq id } ?: throw NotFoundException("Не найден")
            ship.delete()
        }
    }

    fun getCountShip(): Int {
        return database.sequenceOf(ShipTable).count()
    }
}
