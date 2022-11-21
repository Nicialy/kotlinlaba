package ru.ac.uniyar.quires

import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.insertAndGenerateKey
import org.ktorm.entity.count
import org.ktorm.entity.drop
import org.ktorm.entity.filter
import org.ktorm.entity.filterNot
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.take
import org.ktorm.entity.toList
import ru.ac.uniyar.database.BidTable
import ru.ac.uniyar.database.DBBidEntity
import ru.ac.uniyar.utils.NotFoundException

class BidDB(private val database: Database) {

    fun createBid(user_id: Long, role: String, description: String): Any {
        val id = database.insertAndGenerateKey(BidTable) {
            set(it.role, role)
            set(it.user_id, user_id)
            set(it.description, description)
            set(it.status, "В ожидании")
        }
        return id
    }
    fun getAllBid(status: String, page: Int, size: Int): List<DBBidEntity> {
        return database.sequenceOf(BidTable).filter { it.status eq status }.drop((page - 1) * size).take(size).toList()
    }

    fun acceptBid(id: Long) {
        database.useTransaction {
            val bid = database.sequenceOf(BidTable).find { it.id eq id } ?: throw NotFoundException("Не найдено")
            bid.status = "Одобрено"
            bid.user_id.role = bid.role
            bid.user_id.flushChanges()
            bid.flushChanges()
        }
    }
    fun cancelBid(id: Long) {
        database.useTransaction {
            val bid = database.sequenceOf(BidTable, withReferences = false).find { it.id eq id } ?: throw NotFoundException("Не найдено")
            bid.status = "Отказано"
            bid.flushChanges()
        }
    }
    fun delMyBid(user_id: Long, bid_id: Long) {
        database.useTransaction {
            val bid = database.sequenceOf(BidTable).filterNot { it.status eq "В ожидании" }.filter { it.user_id eq user_id }.find { it.id eq bid_id } ?: throw NotFoundException("Не найдено")
            bid.delete()
        }
    }
    fun getCountBid(filter: String): Int {
        return database.sequenceOf(BidTable).filter { it.status eq filter }.count()
    }
}
