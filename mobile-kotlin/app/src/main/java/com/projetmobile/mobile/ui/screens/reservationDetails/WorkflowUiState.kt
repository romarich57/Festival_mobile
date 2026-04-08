/**
 * Rôle : Représente l'état complet (données actuelles, statut validation/rejet et erreurs) de l'interface du workflow.
 *
 * Précondition : Doit être initialisé à la valeur par défaut pour démarrer puis émis par `WorkflowViewModel`.
 *
 * Postcondition : Cet état constant et immuable pilotera les changements réels dans le layout Jetpack Compose.
 */
package com.projetmobile.mobile.ui.screens.reservationDetails

import com.projetmobile.mobile.data.remote.reservation.WorkflowDto

/**
 * Rôle : Définit le contrat du module les détails de réservation.
 */
sealed interface WorkflowUiState {
    /**
     * Rôle : Expose un singleton de support pour le module les détails de réservation.
     */
    object Loading : WorkflowUiState
    /**
     * Rôle : Décrit le composant success du module les détails de réservation.
     */
    data class Success(
        val workflow: WorkflowDto,
        val isSaving: Boolean = false,
        val userMessage: String? = null
    ) : WorkflowUiState
    /**
     * Rôle : Décrit le composant erreur du module les détails de réservation.
     */
    data class Error(val message: String) : WorkflowUiState
}
