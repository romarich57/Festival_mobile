/**
 * Rôle : Contient des utilitaires pour le formatage des dates.
 * Fournit des extensions pour formater des chaînes au format ISO 8601 en chaînes lisibles (FR).
 * Précondition : Le projet doit configurer le "desugaring" (API < 26) si besoin.
 * Postcondition : Formatage de date uniformisé sans crasher l'app en cas d'erreur.
 */
package com.projetmobile.mobile.ui.utils
Rôle : Formate une date ISO 8601 (ex: "2025-08-21T22:00:00.000Z") en "21/08/2025".
 * Supporté sur API < 26 grâce au desugaring configuré dans build.gradle.kts.
 * Précondition : La chaîne en entrée doit être idéalement une date ISO 8601 valide.
 * Postcondition : Renvoie la date formatée ou la chaîne d'origine en guise de fallback (en cas d'erreur)
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
