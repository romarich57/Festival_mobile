package com.projetmobile.mobile.ui.screens.profile

import java.util.Locale

internal fun String.toDisplayRole(): String {
    return when (lowercase(Locale.ROOT)) {
        "admin" -> "Admin"
        "organizer" -> "Organisateur"
        "super-organizer" -> "Super organisateur"
        "benevole" -> "Bénévole"
        else -> replaceFirstChar { character ->
            if (character.isLowerCase()) {
                character.titlecase(Locale.ROOT)
            } else {
                character.toString()
            }
        }
    }
}
