/**
 * Rôle : Logique métier et validation derrière l'écran du nouveau mot de passe.
 * S'assure que le jeton (token) n'est pas vide et que la confirmation de mot de passe est exacte avant de contacter l'API.
 * Précondition : Le ViewModel doit être initialisé à l'aide de sa `Factory` pour recevoir le jeton Deep Link.
 * Postcondition : Informe la vue d'un succès (pour blocage des champs textuels) ou retourne les erreurs.
 */
package com.projetmobile.mobile.ui.screens.auth.resetpassword

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

class ResetPasswordViewModel(
    private val authRepository: AuthRepository,
    initialToken: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ResetPasswordUiState(
            token = initialToken.orEmpty().trim(),
            errorMessage = if (initialToken.isNullOrBlank()) {
                "Le lien de réinitialisation est invalide ou incomplet."
            } else {
                null
            },
        ),
    )
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onPasswordChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                password = value,
                passwordError = null,
                errorMessage = state.errorMessage?.takeIf { state.isTokenMissing },
                successMessage = null,
            )
        }
    }

    fun onConfirmationChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                confirmation = value,
                confirmationError = null,
                errorMessage = state.errorMessage?.takeIf { state.isTokenMissing },
                successMessage = null,
            )
        }
    }

    fun submitPasswordReset() {
        val currentState = _uiState.value
        if (currentState.isTokenMissing) {
            _uiState.update { state ->
                state.copy(errorMessage = "Le lien de réinitialisation est invalide ou incomplet.")
            }
            return
        }

        val validation = AuthFormValidator.validateResetPassword(
            password = currentState.password,
            confirmation = currentState.confirmation,
        )
        if (validation.isInvalid) {
            _uiState.update { state ->
                state.copy(
                    passwordError = validation.passwordError,
                    confirmationError = validation.confirmationError,
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
            authRepository.resetPassword(
                token = currentState.token,
                password = currentState.password,
            ).onSuccess { message ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        password = "",
                        confirmation = "",
                        errorMessage = null,
                        successMessage = message,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = throwable.localizedMessage ?: "Réinitialisation impossible.",
                        successMessage = null,
                    )
                }
            }
        }
    }

    companion object {
        fun factory(
            authRepository: AuthRepository,
            initialToken: String?,
        ): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    ResetPasswordViewModel(authRepository, initialToken)
                }
            }
        }
    }
}
