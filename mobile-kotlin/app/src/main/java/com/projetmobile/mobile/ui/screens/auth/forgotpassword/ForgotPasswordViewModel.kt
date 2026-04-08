/**
 * Rôle : Ordonne la logique fonctionnelle de la réinitialisation du mot de passe.
 * S'assure que l'email est correctement formaté via l'`AuthFormValidator` avant d'attaquer l'API.
 * Précondition : Un `AuthRepository` d'interface au serveur HTTP doit être fourni via la Factory.
 * Postcondition : Émet des objets `ForgotPasswordUiState` successifs (Chargement, puis Succès ou Erreur).
 */
package com.projetmobile.mobile.ui.screens.auth.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import com.projetmobile.mobile.ui.utils.validation.AuthFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rôle : Porte l'état et la logique du module l'authentification.
 */
class ForgotPasswordViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    /**
     * Rôle : Gère la modification du champ email.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onEmailChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                email = value,
                emailError = null,
                errorMessage = null,
                successMessage = null,
            )
        }
    }

    /**
     * Rôle : Exécute l'action submit mot de passe réinitialisation demande du module l'authentification.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun submitPasswordResetRequest() {
        val currentState = _uiState.value
        val validation = AuthFormValidator.validateForgotPassword(currentState.email)
        if (validation.isInvalid) {
            _uiState.update { state ->
                state.copy(
                    emailError = validation.emailError,
                    errorMessage = null,
                    successMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null, successMessage = null)
            }
            authRepository.requestPasswordReset(currentState.email.trim())
                .onSuccess { message ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = null,
                            successMessage = message,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = throwable.localizedMessage
                                ?: "Demande de réinitialisation impossible.",
                            successMessage = null,
                        )
                    }
                }
        }
    }

    /**
     * Rôle : Expose un singleton de support pour le module l'authentification.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module l'authentification.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    ForgotPasswordViewModel(authRepository)
                }
            }
        }
    }
}
