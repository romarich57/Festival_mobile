package com.projetmobile.mobile.data.entity.festival

private val FestivalManagementRoles = setOf(
    "admin",
    "super-organizer",
    "organizer",
)

private val FestivalDeleteRoles = setOf(
    "admin",
    "super-organizer",
)

fun canManageFestivals(role: String?): Boolean {
    return role?.trim()?.lowercase() in FestivalManagementRoles
}

fun canDeleteFestivals(role: String?): Boolean {
    return role?.trim()?.lowercase() in FestivalDeleteRoles
}
