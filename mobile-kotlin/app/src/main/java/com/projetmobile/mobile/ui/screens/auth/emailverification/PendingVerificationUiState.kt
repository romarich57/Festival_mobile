/**
 * Rôle : DTO (Data Transfer Object) immuable modélisant l'état de la vue de pending verification.
 * Capture l'email vers lequel le lien a été envoyé, et les variables pour l'UI de chargement.
 * Précondition : Mis à jour exclusivement par le bloc métier.
 * Postcondition : Utilisé par la vue pour afficher/masquer le loader et les labels d'erreur.
 */
package com.projetmobile.mobile.ui.screens.auth.emailverification

/**
 * Rôle : Décrit l'état immuable du module l'authentification emailverification.
 */
data class PendingVerificationUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
