package com.projetmobile.mobile.ui.screens.admin.shared

val ADMIN_AVAILABLE_ROLES = listOf("benevole", "organizer", "super-organizer", "admin")

fun roleDisplayName(role: String): String = when (role) {
    "benevole" -> "Bénévole"
    "organizer" -> "Organisateur"
    "super-organizer" -> "Super-organisateur"
    "admin" -> "Admin"
    else -> role.replaceFirstChar { it.uppercase() }
}
