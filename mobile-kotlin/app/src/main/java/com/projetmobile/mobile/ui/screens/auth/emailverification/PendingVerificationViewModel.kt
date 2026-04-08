/**
 * Rôle : Manipulateur d'état concernant la page de renvoi d'email (PendingVerification).
 * Assure la résolution asynchrone du dernier email enregistré par l'app en cas d'absence.
 * Précondition : Hérite du couplage à `AuthRepository` pour piloter la source de données distante.
 * Postcondition : Soumet la demande de re-transmission de mail au backend et capte la réponse en Live-data.
 */
package com.projetmobile.mobile.ui.screens.auth.emailverification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PendingVerificationViewModel(
    private val authRepository: AuthRepository,
    initialEmail: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PendingVerificationUiState(email = initialEmail.orEmpty()))
    val uiState: StateFlow<PendingVerificationUiState> = _uiState.asStateFlow()

    init {
        if (initialEmail.isNullOrBlank()) {
            viewModelScope.launch {
                val pendingEmail = authRepository.getPendingVerificationEmail().orEmpty()
                _uiState.update { state -> state.copy(email = pendingEmail) }
            }
        }
    }

    fun resendVerification() {
        val email = _uiState.value.email.trim()
        if (email.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = "Aucun email en attente de vérification.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null, infoMessage = null)
            }
            authRepository.resendVerification(email)
                .onSuccess { message ->
                    _uiState.update { state ->
                        state.copy(isLoading = false, infoMessage = message)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = throwable.localizedMessage ?: "Renvoi impossible.",
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository, initialEmail: String?): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    PendingVerificationViewModel(authRepository, initialEmail)
                }
            }
        }
    }
}
