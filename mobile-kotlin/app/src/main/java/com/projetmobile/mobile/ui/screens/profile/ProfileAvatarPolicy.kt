/**
 * Rôle : Permet de gérer la logique d'affichage ou de changement de la photo de profil.
 *
 * Précondition : Identifie les contraintes MIME et dimensions max.
 *
 * Postcondition : Fournit un composant avatar cliquable (image picker).
 */
package com.projetmobile.mobile.ui.screens.profile

import com.projetmobile.mobile.data.entity.auth.AuthUser

private const val MAX_AVATAR_SIZE_BYTES = 2 * 1024 * 1024

/**
 * Rôle : Valide avatar selection.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun validateAvatarSelection(selection: AvatarSelectionPayload): String? {
    val normalizedMimeType = selection.mimeType.lowercase()
    if (!normalizedMimeType.startsWith("image/")) {
        return "Seules les images sont acceptées pour l'avatar."
    }
    if (selection.bytes.size > MAX_AVATAR_SIZE_BYTES) {
        return "L'image dépasse 2 Mo."
    }
    return null
}

/**
 * Rôle : Transforme une sélection locale d'avatar en état UI exploitable par le profil.
 *
 * Précondition : `selection` doit déjà avoir passé la validation de type et de taille.
 *
 * Postcondition : Retourne un état local contenant le nom de fichier, le MIME type et les octets à téléverser.
 */
internal fun AvatarSelectionPayload.toAvatarState(): ProfileAvatarState.LocalSelection {
    return ProfileAvatarState.LocalSelection(
        fileName = fileName,
        mimeType = mimeType,
        bytes = bytes,
        previewUriString = previewUriString,
    )
}

/**
 * Rôle : Exécute l'action next avatar état after removal du module le profil.
 *
 * Précondition : Les dépendances nécessaires à l'opération doivent être disponibles.
 *
 * Postcondition : Le résultat reflète l'opération demandée.
 */
internal fun nextAvatarStateAfterRemoval(
    profile: AuthUser?,
    avatarState: ProfileAvatarState,
): ProfileAvatarState {
    return when {
        profile?.avatarUrl != null -> ProfileAvatarState.Removed
        avatarState is ProfileAvatarState.LocalSelection -> ProfileAvatarState.Unchanged
        else -> ProfileAvatarState.Unchanged
    }
}
