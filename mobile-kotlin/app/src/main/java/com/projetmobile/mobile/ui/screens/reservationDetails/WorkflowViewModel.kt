/**
 * Rôle : ViewModel pilotant la dynamique de validation (statuts du workflow) pour une réservation ou un jeu.
 *
 * Précondition : Le repository de workflow correspondant doit être fonctionnel et fournir l'état courant.
 *
 * Postcondition : Met à jour son flux d'état (UiState) et transmet les résultats des requêtes au frontend de l'UI.
 */
package com.projetmobile.mobile.ui.screens.reservationDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.remote.reservation.WorkflowUpdatePayload
import com.projetmobile.mobile.data.repository.toRepositoryException
import com.projetmobile.mobile.data.repository.festival.FestivalRepository
import com.projetmobile.mobile.data.repository.workflow.WorkflowRepository
import com.projetmobile.mobile.ui.screens.festivalForm.FestivalFormViewModel
import kotlinx.coroutines.launch

/**
 * Rôle : Porte l'état et la logique du module les détails de réservation.
 */
class WorkflowViewModel(
    private val repository: WorkflowRepository
) : ViewModel() {

    var uiState: WorkflowUiState by mutableStateOf(WorkflowUiState.Loading)
        private set

    // Charger les données (Appelé quand l'onglet s'affiche)
    /**
     * Rôle : Charge workflow.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun loadWorkflow(reservationId: Int) {
        viewModelScope.launch {
            uiState = WorkflowUiState.Loading
            try {
                val result = repository.getWorkflow(reservationId)
                uiState = WorkflowUiState.Success(workflow = result)
            } catch (throwable: Throwable) {
                uiState = WorkflowUiState.Error(
                    throwable.toRepositoryException("Impossible de charger le workflow.")
                        .localizedMessage
                        ?: "Impossible de charger le workflow.",
                )
            }
        }
    }

    // Sauvegarder les changements
    /**
     * Rôle : Exécute l'action mise à jour workflow du module les détails de réservation.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun updateWorkflow(id: Int, payload: WorkflowUpdatePayload) {
        val currentState = uiState as? WorkflowUiState.Success ?: return

        viewModelScope.launch {
            uiState = currentState.copy(isSaving = true)
            try {
                val updated = repository.updateWorkflow(id, payload)
                uiState = WorkflowUiState.Success(
                    workflow = updated,
                    userMessage = "Enregistré avec succès !"
                )
            } catch (throwable: Throwable) {
                uiState = currentState.copy(
                    isSaving = false,
                    userMessage = throwable.toRepositoryException("Erreur de sauvegarde.")
                        .localizedMessage
                        ?: "Erreur de sauvegarde",
                )
            }
        }
    }

    /**
     * Rôle : Réinitialise message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun clearMessage() {
        (uiState as? WorkflowUiState.Success)?.let {
            uiState = it.copy(userMessage = null)
        }
    }

    /**
     * Rôle : Expose un singleton de support pour le module les détails de réservation.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module les détails de réservation.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
        fun factory(workflowRepository: WorkflowRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { WorkflowViewModel(workflowRepository) }
            }
    }
}
