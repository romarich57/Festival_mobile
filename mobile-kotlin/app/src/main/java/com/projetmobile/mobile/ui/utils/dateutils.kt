package com.projetmobile.mobile.ui.utils

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formate une date ISO 8601 (ex: "2025-08-21T22:00:00.000Z") en "21/08/2025".
 * Supporté sur API < 26 grâce au desugaring configuré dans build.gradle.kts.
 */
fun formatDate(isoDate: String): String {
    return try {
        val parsed = OffsetDateTime.parse(isoDate)
        parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE))
    } catch (e: Exception) {
        isoDate // fallback : affiche la date brute plutôt que crasher
    }
}
