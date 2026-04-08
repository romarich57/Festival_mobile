/**
 * Rôle : Décrit l'état UI immuable du module l'administration formulaire.
 */

package com.projetmobile.mobile.ui.screens.admin.form

import com.projetmobile.mobile.data.entity.auth.AuthUser

/**
 * Rôle : Définit le contrat du module l'administration formulaire.
 */
sealed interface AdminUserFormMode {
    /**
     * Rôle : Expose un singleton de support pour le module l'administration formulaire.
     */
    data object Create : AdminUserFormMode
    /**
     * Rôle : Décrit le composant édition du module l'administration formulaire.
     */
    data class Edit(val userId: Int) : AdminUserFormMode
}

/**
 * Rôle : Décrit l'état immuable du module l'administration formulaire.
 */
data class AdminUserFormState(
    val login: String = "",
    val password: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "benevole",
    val emailVerified: Boolean = false,
    // Field-level errors
    val loginError: String? = null,
    val passwordError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
)

/**
 * Rôle : Décrit l'état du formulaire complet de la page `AdminUserFormScreen`.
 *
 * Précondition : Met à disposition des erreurs liées à chaque champ de manière asynchrone par validation.
 *
 * Postcondition : Structure de donnée lisible pour rendre un éditeur ou un créateur `AdminUserFormMode`.
 */
data class AdminUserFormUiState(
    val mode: AdminUserFormMode = AdminUserFormMode.Create,
    val initialUser: AuthUser? = null,
    val form: AdminUserFormState = AdminUserFormState(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false,
)

/**
 * Rôle : Exécute l'action formulaire état from du module l'administration formulaire.
 *
 * Précondition : Les données du module doivent être disponibles pour initialiser ou exposer l'état.
 *
 * Postcondition : L'objet retourné décrit un état cohérent et immuable.
 */
internal fun formStateFrom(user: AuthUser) = AdminUserFormState(
    login = user.login,
    firstName = user.firstName,
    lastName = user.lastName,
    email = user.email,
    phone = user.phone ?: "",
    role = user.role,
    emailVerified = user.emailVerified,
)
