package com.projetmobile.mobile.ui.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Formate une date ISO 8601 (ex: "2025-08-21T22:00:00.000Z") en "21/08/2025".
 * Retourne la chaîne brute si le parsing échoue (fail-safe).
 */
@RequiresApi(Build.VERSION_CODES.O)
fun formatDate(isoDate: String): String {
    return try {
        val parsed = OffsetDateTime.parse(isoDate)
        parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.FRANCE))
    } catch (e: Exception) {
        isoDate // fallback : affiche la date brute plutôt que crasher
    }
}
