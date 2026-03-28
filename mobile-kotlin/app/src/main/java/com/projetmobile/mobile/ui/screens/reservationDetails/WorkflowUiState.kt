package com.projetmobile.mobile.ui.screens.reservationDetails

import com.projetmobile.mobile.data.remote.reservation.WorkflowDto

sealed interface WorkflowUiState {
    object Loading : WorkflowUiState
    data class Success(
        val workflow: WorkflowDto,
        val isSaving: Boolean = false,
        val userMessage: String? = null
    ) : WorkflowUiState
    data class Error(val message: String) : WorkflowUiState
}