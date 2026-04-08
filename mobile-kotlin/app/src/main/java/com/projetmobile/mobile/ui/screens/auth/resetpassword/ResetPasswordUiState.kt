/**
 * Rôle : Représente l'état immuable du processus de finalisation d'un mot de passe oublié.
 * Inclut le jeton (`token`) sécurisé récupéré depuis l'URL, les valeurs saisies par l'utilisateur et les messages contextuels.
 * Précondition : Un `token` cryptographique doit initialement peupler ce state.
 * Postcondition : Contient les informations nécessaires pour verrouiller l'interface ou valider l'action.
 */
package com.projetmobile.mobile.ui.screens.auth.resetpassword

/**
 * Rôle : Décrit l'état immuable du module l'authentification.
 */
data class ResetPasswordUiState(
    val token: String = "",
    val password: String = "",
    val confirmation: String = "",
    val passwordError: String? = null,
    val confirmationError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val isTokenMissing: Boolean
        get() = token.isBlank()

    val isSubmitEnabled: Boolean
        get() = !isLoading && !isTokenMissing && successMessage == null
}
