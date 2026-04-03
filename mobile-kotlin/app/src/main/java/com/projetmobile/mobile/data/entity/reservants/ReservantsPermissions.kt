package com.projetmobile.mobile.data.entity.reservants

private val ReservantManagementRoles = setOf(
    "admin",
    "super-organizer",
    "organizer",
)

private val ReservantDeleteRoles = setOf(
    "admin",
    "super-organizer",
)

fun canManageReservants(role: String?): Boolean {
    return role?.trim()?.lowercase() in ReservantManagementRoles
}

fun canDeleteReservants(role: String?): Boolean {
    return role?.trim()?.lowercase() in ReservantDeleteRoles
}
