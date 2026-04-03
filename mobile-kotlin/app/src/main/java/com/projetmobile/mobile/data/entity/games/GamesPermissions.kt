package com.projetmobile.mobile.data.entity.games

private val GameManagementRoles = setOf(
    "admin",
    "super-organizer",
    "organizer",
)

fun canManageGames(role: String?): Boolean {
    return role?.trim()?.lowercase() in GameManagementRoles
}

fun canUploadGameImages(role: String?): Boolean = canManageGames(role)
