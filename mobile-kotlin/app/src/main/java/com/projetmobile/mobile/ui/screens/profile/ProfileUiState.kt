/**
 * Rôle : Décrit l'état UI immuable du module le profil.
 */

package com.projetmobile.mobile.ui.screens.profile

import com.projetmobile.mobile.data.entity.auth.AuthUser

/**
 * Rôle : Décrit l'état immuable du module le profil.
 */
data class ProfileFormState(
    val login: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val loginError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
)

/**
 * Rôle : Définit le contrat du module le profil.
 */
sealed interface ProfileAvatarState {
    /**
     * Rôle : Expose un singleton de support pour le module le profil.
     */
    data object Unchanged : ProfileAvatarState

    /**
     * Rôle : Expose un singleton de support pour le module le profil.
     */
    data object Removed : ProfileAvatarState

    /**
     * Rôle : Décrit le composant local selection du module le profil.
     */
    data class LocalSelection(
        val fileName: String,
        val mimeType: String,
        val bytes: ByteArray,
        val previewUriString: String,
    ) : ProfileAvatarState
}

/**
 * Rôle : Décrit le composant avatar selection payload du module le profil.
 */
data class AvatarSelectionPayload(
    val fileName: String,
    val mimeType: String,
    val bytes: ByteArray,
    val previewUriString: String,
)

/**
 * Rôle : Décrit l'état asynchrone des informations du profil incluant l'édition conditionnelle, les avatarts, et les suppressions de session.
 *
 * Précondition : Centralise et valide chaque entrée à titre individuel.
 *
 * Postcondition : Affiche dynamiquement les options de rafraîchissement au composant UI [ProfileScreen].
 */
data class ProfileUiState(
    val profile: AuthUser? = null,
    val form: ProfileFormState = ProfileFormState(),
    val avatarState: ProfileAvatarState = ProfileAvatarState.Unchanged,
    val editingFields: Set<ProfileEditableField> = emptySet(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isSaving: Boolean = false,
    val isSendingPasswordReset: Boolean = false,
    val hasPendingChanges: Boolean = false,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
    val pendingSessionUserUpdate: AuthUser? = null,
) {
    val isEditing: Boolean
        get() = editingFields.isNotEmpty()

    /**
     * Rôle : Exécute l'action is champ editing du module le profil.
     *
     * Précondition : Les données du module doivent être disponibles pour initialiser ou exposer l'état.
     *
     * Postcondition : L'objet retourné décrit un état cohérent et immuable.
     */
    fun isFieldEditing(field: ProfileEditableField): Boolean {
        return field in editingFields
    }
}

/**
 * Rôle : Exécute l'action profil formulaire état for du module le profil.
 *
 * Précondition : Les données du module doivent être disponibles pour initialiser ou exposer l'état.
 *
 * Postcondition : L'objet retourné décrit un état cohérent et immuable.
 */
internal fun profileFormStateFor(user: AuthUser?): ProfileFormState {
    return if (user == null) {
        ProfileFormState()
    } else {
        ProfileFormState(
            login = user.login,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            phone = user.phone.orEmpty(),
        )
    }
}
