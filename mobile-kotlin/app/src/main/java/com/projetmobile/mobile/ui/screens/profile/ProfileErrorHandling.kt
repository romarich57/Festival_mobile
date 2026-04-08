/**
 * Rôle : Mapper et gestionnaire d'erreurs d'interface liées à un échec de réseau sur le profil.
 *
 * Précondition : Capte tout blocage backend via try/catch.
 *
 * Postcondition : Construit des structures Toast/Snackbar d'erreur lisibles.
 */
package com.projetmobile.mobile.ui.screens.profile

import java.text.Normalizer
import java.util.Locale

/**
 * Rôle : Traduit une erreur backend du profil en message ou erreur de champ exploitable par l'UI.
 *
 * Précondition : `rawMessage` doit provenir d'un échec repository ou réseau du profil.
 *
 * Postcondition : L'état retourné expose soit une erreur de champ ciblée, soit un message global réconcilié avec l'interface.
 */
internal fun ProfileUiState.withBackendProfileError(rawMessage: String): ProfileUiState {
    val normalized = rawMessage.normalizedForMatching()
    return when {
        normalized.contains("login deja utilise") -> {
            copy(
                form = form.copy(loginError = "Ce pseudo est déjà utilisé."),
                editingFields = editingFields + ProfileEditableField.Login,
                errorMessage = null,
                infoMessage = null,
            ).recalculated()
        }

        normalized.contains("email deja utilise") -> {
            copy(
                form = form.copy(emailError = "Cet email est déjà utilisé."),
                editingFields = editingFields + ProfileEditableField.Email,
                errorMessage = null,
                infoMessage = null,
            ).recalculated()
        }

        else -> copy(
            errorMessage = rawMessage,
            infoMessage = null,
        ).recalculated()
    }
}

/**
 * Rôle : Normalise une chaîne pour faciliter la détection de motifs d'erreur backend.
 *
 * Précondition : La chaîne d'entrée peut contenir des accents, une casse variable ou des séparateurs.
 *
 * Postcondition : Retourne une version sans diacritiques, en minuscules, adaptée aux comparaisons textuelles simples.
 */
private fun String.normalizedForMatching(): String {
    return Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .lowercase(Locale.ROOT)
}
