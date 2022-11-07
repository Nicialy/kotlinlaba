package ru.ac.uniyar.database

import org.ktorm.schema.Table
import org.ktorm.schema.date
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.text
import org.ktorm.schema.varchar

object UsersTable : Table<DBUserEntity>("USERS") {
    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val nickname = varchar("nickname").bindTo { it.nickname }
    val role = varchar("role").bindTo { it.role }
    val password = text("password").bindTo { it.password }
    val surname = varchar("surname").bindTo { it.surname }
    val middle_name = varchar("middle_name").bindTo { it.middle_name }
}

object ShipTable : Table<DBShipEntity>("SHIP") {
    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val description = text("description").bindTo { it.description }
    val status = varchar("status").bindTo { it.status }
    val url = text("url").bindTo { it.url }
    val countPlace = int("count_places").bindTo { it.countPlace }
}

object TravelTable : Table<DBTravelEntity>("TRAVEL") {
    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val description = text("description").bindTo { it.description }
    val status = varchar("status").bindTo { it.status }
    val date_start = date("date_start").bindTo { it.date_start }
    val date_end = date("date_end").bindTo { it.date_end }
    val ship_id = long("ship_id").references(ShipTable) { it.ship_id }
}

object InvitationTable : Table<DBInvitationEntity>("INVITATION") {
    val id = long("id").primaryKey().bindTo { it.id }
    val travel_id = long("travel_id").references(TravelTable) { it.travel_id }
    val user_id = long("user_id").references(UsersTable) { it.user_id }
    val status = varchar("status").bindTo { it.status }
}
object CrewTable : Table<DBCrewEntity>("CREW") {
    val id = long("id").primaryKey().bindTo { it.id }
    val travel_id = long("travel_id").references(TravelTable) { it.travel_id }
    val user_id = long("user_id").references(UsersTable) { it.user_id }
}
object BidTable : Table<DBBidEntity>("BID") {
    val id = long("id").primaryKey().bindTo { it.id }
    val user_id = long("user_id").references(UsersTable) { it.user_id }
    val role = varchar("role").bindTo { it.role }
    val description = text("description").bindTo { it.description }
    val status = varchar("status").bindTo { it.status }
}

object TeamVisitorTable : Table<DBTeamVisitorEntity>("TEAM_VISITOR") {
    val id = long("id").primaryKey().bindTo { it.id }
    val travel_id = long("travel_id").references(TravelTable) { it.travel_id }
    val user_id = long("user_id").references(UsersTable) { it.user_id }
}
