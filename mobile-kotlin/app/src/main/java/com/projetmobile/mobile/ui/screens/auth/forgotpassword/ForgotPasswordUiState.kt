/**
 * Rôle : Représente l'état immuable du formulaire de demande de réinitialisation de mot de passe.
 * Inclut l'adresse email candidate, l'indicateur de chargement réseau et les retours d'API.
 * Précondition : Constamment mis à jour par le `ForgotPasswordViewModel`.
 * Postcondition : Lu par le composant racine `ForgotPasswordScreen` pour modifier l'affichage en temps réel.
 */
package com.projetmobile.mobile.ui.screens.auth.forgotpassword

/**
 * Rôle : Décrit l'état immuable du module l'authentification.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
)
