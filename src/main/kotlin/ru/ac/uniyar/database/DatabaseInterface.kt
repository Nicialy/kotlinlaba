package ru.ac.uniyar.database

import org.ktorm.entity.Entity
import java.time.LocalDate

interface DBUserEntity : Entity<DBUserEntity> {
    val id: Long
    val name: String
    val nickname: String
    var role: String
    val password: String
    val surname: String
    val middle_name: String
}

interface DBShipEntity : Entity<DBShipEntity> {
    companion object : Entity.Factory<DBShipEntity>()
    val id: Long
    var countPlace: Int
    val name: String
    val description: String
    val status: String
    val url: String
}

interface DBTravelEntity : Entity<DBTravelEntity> {
    companion object : Entity.Factory<DBTravelEntity>()
    val id: Long
    val name: String
    val description: String
    var status: String
    val date_start: LocalDate
    val date_end: LocalDate
    val ship_id: DBShipEntity
}

interface DBInvitationEntity : Entity<DBInvitationEntity> {
    companion object : Entity.Factory<DBInvitationEntity>()
    val id: Long
    val travel_id: DBTravelEntity
    val user_id: DBUserEntity
    var status: String
}

interface DBCrewEntity : Entity<DBCrewEntity> {
    companion object : Entity.Factory<DBCrewEntity>()
    val id: Long
    val travel_id: DBTravelEntity
    val user_id: DBUserEntity
}
interface DBBidEntity : Entity<DBBidEntity> {
    companion object : Entity.Factory<DBBidEntity>()
    val id: Long
    val user_id: DBUserEntity
    var role: String
    val description: String
    var status: String
}
interface DBTeamVisitorEntity : Entity<DBTeamVisitorEntity> {
    companion object : Entity.Factory<DBTeamVisitorEntity>()
    val id: Long
    val travel_id: DBTravelEntity
    val user_id: DBUserEntity
}
data class CustomTravel(
    val id: Long,
    val name: String,
    val description: String,
    val status: String,
    val date_start: LocalDate,
    val date_end: LocalDate,
    val ship_id: DBShipEntity,
    val user: List<DBUserEntity>
)

