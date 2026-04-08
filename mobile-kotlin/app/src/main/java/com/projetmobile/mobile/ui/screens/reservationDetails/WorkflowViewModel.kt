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

class WorkflowViewModel(
    private val repository: WorkflowRepository
) : ViewModel() {

    var uiState: WorkflowUiState by mutableStateOf(WorkflowUiState.Loading)
        private set

    // Charger les données (Appelé quand l'onglet s'affiche)
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

    fun clearMessage() {
        (uiState as? WorkflowUiState.Success)?.let {
            uiState = it.copy(userMessage = null)
        }
    }

    companion object {
        fun factory(workflowRepository: WorkflowRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { WorkflowViewModel(workflowRepository) }
            }
    }
}
