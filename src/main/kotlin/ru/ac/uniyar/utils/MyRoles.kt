package ru.ac.uniyar.utils

import kotlin.reflect.KFunction

class MyRoles {
    val mapHandler = mapOf<String, KFunction<Boolean>>(
        "travelCreate" to this::travelCreatePermission,
        "bidAccept" to this::bidAcceptPermission,
        "registration" to this::registrationPermission,
        "bidCreate" to this::bidCreatePermission,
        "invitation" to this::invitationPermission,
        "logout" to this::logoutPermission,
        "login" to this::loginPermission,
        "shipAdd" to this::shipPermission,
        "visit" to this::visitPermission
    )

    fun travelCreatePermission(role: RolePermission): Boolean {
        return role.travelCreate
    }
    fun bidCreatePermission(role: RolePermission): Boolean {
        return role.bidCreate
    }
    fun bidAcceptPermission(role: RolePermission): Boolean {
        return role.bidAccept
    }
    fun registrationPermission(role: RolePermission): Boolean {
        return role.registration
    }
    fun invitationPermission(role: RolePermission): Boolean {
        return role.invitation
    }
    fun logoutPermission(role: RolePermission): Boolean {
        return role.logOut
    }
    fun loginPermission(role: RolePermission): Boolean {
        return role.login
    }
    fun shipPermission(role: RolePermission): Boolean {
        return role.shipCreate
    }
    fun visitPermission(role: RolePermission): Boolean{
        return  role.visit
    }
}

class RolePermission(
    val shipCreate: Boolean,
    val registration: Boolean,
    val login: Boolean,
    val logOut: Boolean,
    val travelCreate: Boolean,
    val invitation: Boolean,
    val travelCrew: Boolean,
    val bidAccept: Boolean,
    val bidCreate: Boolean,
    val visit: Boolean
)

val ADMIN = RolePermission(
    shipCreate = true,
    registration = false,
    login = false,
    logOut = true,
    travelCreate = true,
    invitation = true,
    travelCrew = true,
    bidAccept = true,
    bidCreate = false,
    visit = true
)

val CAPTAIN = RolePermission(
    shipCreate = false,
    registration = false,
    login = false,
    logOut = true,
    travelCreate = true,
    invitation = true,
    travelCrew = true,
    bidAccept = false,
    bidCreate = true,
    visit = true
)

val BOATSWAIN = RolePermission(
    shipCreate = false,
    registration = false,
    login = false,
    logOut = true,
    travelCreate = false,
    invitation = true,
    travelCrew = true,
    bidAccept = false,
    bidCreate = true,
    visit = true
)

val VISITOR = RolePermission(
    shipCreate = false,
    registration = false,
    login = false,
    logOut = true,
    travelCreate = false,
    invitation = true,
    travelCrew = false,
    bidAccept = false,
    bidCreate = true,
    visit = true
)

val GUEST = RolePermission(
    shipCreate = false,
    registration = true,
    login = true,
    logOut = false,
    travelCreate = false,
    invitation = false,
    travelCrew = false,
    bidAccept = false,
    bidCreate = false,
    visit = false
)

val ANOUTHERROLE = RolePermission(
    shipCreate = false,
    registration = false,
    login = false,
    logOut = true,
    travelCreate = false,
    invitation = true,
    travelCrew = false,
    bidAccept = false,
    bidCreate = true,
    visit = true
)
