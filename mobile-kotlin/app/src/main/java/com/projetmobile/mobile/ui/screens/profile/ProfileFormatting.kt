/**
 * Rôle : Gère le formatage spécifique requis pour les variables du profil.
 *
 * Précondition : Prend des datas brutes.
 *
 * Postcondition : Renvoie un affichage lisible et formatté ou masqué selon le champ.
 */
package com.projetmobile.mobile.ui.screens.profile

import java.util.Locale

/**
 * Rôle : Formate un identifiant de rôle backend en libellé utilisateur lisible.
 *
 * Précondition : La chaîne reçue doit correspondre à un code de rôle ou à un texte déjà exploitable.
 *
 * Postcondition : Retourne un libellé normalisé prêt à être affiché dans l'interface de profil.
 */
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
