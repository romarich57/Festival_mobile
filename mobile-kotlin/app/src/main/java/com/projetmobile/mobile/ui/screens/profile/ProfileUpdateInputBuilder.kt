/**
 * Rôle : Assiste dans la création et l'envoi du formulaire de modification de profil utilisateur.
 *
 * Précondition : Possède la nouvelle base de texte ou avatar à envoyer au serveur.
 *
 * Postcondition : Transforme l'état local en objet réseau complet pour les APIS.
 */
package com.projetmobile.mobile.ui.screens.profile

import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.entity.profile.OptionalField
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateInput

/**
 * Rôle : Recalcule les drapeaux dérivés du profil après une modification locale.
 *
 * Précondition : L'état courant doit déjà contenir le profil, le formulaire et l'état d'avatar à comparer.
 *
 * Postcondition : Le champ `hasPendingChanges` reflète précisément la différence entre l'état sauvegardé et l'édition en cours.
 */
internal fun ProfileUiState.recalculated(): ProfileUiState {
    return copy(hasPendingChanges = hasPendingProfileChanges(profile, form, avatarState))
}

/**
 * Rôle : Indique si en attente profil changes est présent.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun hasPendingProfileChanges(
    profile: AuthUser?,
    form: ProfileFormState,
    avatarState: ProfileAvatarState,
): Boolean {
    if (profile == null) {
        return false
    }

    val phoneValue = form.phone.trim().ifBlank { null }
    val textChanged = profile.login != form.login.trim() ||
        profile.firstName != form.firstName.trim() ||
        profile.lastName != form.lastName.trim() ||
        profile.email != form.email.trim() ||
        profile.phone != phoneValue

    val avatarChanged = when (avatarState) {
        ProfileAvatarState.Unchanged -> false
        ProfileAvatarState.Removed -> profile.avatarUrl != null
        is ProfileAvatarState.LocalSelection -> true
    }

    return textChanged || avatarChanged
}

/**
 * Rôle : Construit profil mise à jour input.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun buildProfileUpdateInput(
    savedProfile: AuthUser,
    form: ProfileFormState,
    avatarState: ProfileAvatarState,
    uploadedAvatarUrl: String?,
): ProfileUpdateInput? {
    val normalizedLogin = form.login.trim()
    val normalizedFirstName = form.firstName.trim()
    val normalizedLastName = form.lastName.trim()
    val normalizedEmail = form.email.trim()
    val normalizedPhone = form.phone.trim().ifBlank { null }

    val input = ProfileUpdateInput(
        login = normalizedLogin.takeIf { it != savedProfile.login },
        firstName = normalizedFirstName.takeIf { it != savedProfile.firstName },
        lastName = normalizedLastName.takeIf { it != savedProfile.lastName },
        email = normalizedEmail.takeIf { it != savedProfile.email },
        phone = if (normalizedPhone != savedProfile.phone) {
            OptionalField.Value(normalizedPhone)
        } else {
            OptionalField.Unchanged
        },
        avatarUrl = when (avatarState) {
            ProfileAvatarState.Unchanged -> OptionalField.Unchanged
            ProfileAvatarState.Removed -> OptionalField.Value(null)
            is ProfileAvatarState.LocalSelection -> OptionalField.Value(uploadedAvatarUrl)
        },
    )

    return if (
        input.login == null &&
        input.firstName == null &&
        input.lastName == null &&
        input.email == null &&
        input.phone == OptionalField.Unchanged &&
        input.avatarUrl == OptionalField.Unchanged
    ) {
        null
    } else {
        input
    }
}
