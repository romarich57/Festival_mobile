/**
 * Rôle : Gère la logique métier et l'état interactif de l'écran de connexion.
 * Intercepte les saisies utilisateur, réalise les validations et exécute les appels vers l'`AuthRepository`.
 * Précondition : Lié au cycle de vie de `LoginScreen` au sein de Jetpack Compose.
 * Postcondition : Met à jour le StateFlow `uiState` en réaction aux entrées et au résultat des appels réseau.
 */
package com.projetmobile.mobile.ui.screens.auth.login

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
class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val lastIdentifier = authRepository.getLastLoginIdentifier()
            if (!lastIdentifier.isNullOrBlank()) {
                _uiState.update { state ->
                    state.copy(identifier = lastIdentifier)
                }
            }
        }
    }

    /**
     * Rôle : Gère la modification du champ identifier.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onIdentifierChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                identifier = value,
                identifierError = null,
                errorMessage = null,
                infoMessage = null,
            )
        }
    }

    /**
     * Rôle : Gère la modification du champ mot de passe.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onPasswordChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                password = value,
                passwordError = null,
                errorMessage = null,
            )
        }
    }

    /**
     * Rôle : Exécute l'action submit identifiant du module l'authentification.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun submitLogin() {
        val currentState = _uiState.value
        val validation = AuthFormValidator.validateLogin(
            identifier = currentState.identifier,
            password = currentState.password,
        )
        if (validation.isInvalid) {
            _uiState.update { state ->
                state.copy(
                    identifierError = validation.identifierError,
                    passwordError = validation.passwordError,
                    errorMessage = null,
                    infoMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null, infoMessage = null)
            }
            authRepository.login(
                identifier = currentState.identifier.trim(),
                password = currentState.password,
            ).onSuccess { user ->
                _uiState.value = LoginUiState(
                    identifier = currentState.identifier.trim(),
                    authenticatedUser = user,
                    infoMessage = "Connexion réussie.",
                )
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = throwable.localizedMessage ?: "Connexion impossible.",
                    )
                }
            }
        }
    }

    /**
     * Rôle : Exécute l'action resend verification du module l'authentification.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun resendVerification() {
        val emailCandidate = _uiState.value.identifier.trim()
        val validation = AuthFormValidator.validateResendVerification(emailCandidate)
        if (validation.isInvalid) {
            _uiState.update { state ->
                state.copy(
                    identifierError = validation.emailError,
                    errorMessage = null,
                    infoMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null, infoMessage = null)
            }
            authRepository.resendVerification(emailCandidate)
                .onSuccess { message ->
                    _uiState.update { state ->
                        state.copy(isLoading = false, infoMessage = message, errorMessage = null)
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

    /**
     * Rôle : Consomme authenticated utilisateur.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun consumeAuthenticatedUser() {
        _uiState.update { state ->
            state.copy(authenticatedUser = null, password = "", isLoading = false)
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
                    LoginViewModel(authRepository)
                }
            }
        }
    }
}
