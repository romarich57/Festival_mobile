/**
 * Rôle : Contrôleur d'interface d'inscription, vérifie et expédie le formulaire vers le serveur.
 * Utilise `AuthFormValidator` pour checker exhaustivement chaque champ (complexité MD5/nombres, email valide). 
 * Précondition : Conservé en mémoire tant que l'écran `RegisterScreen` est affiché.
 * Postcondition : Informe la vue d'un succès via `pendingVerificationEmail` pointant vers validation, ou émet les erreurs par champ.
 */
package com.projetmobile.mobile.ui.screens.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.entity.auth.RegisterAccountInput
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
class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Rôle : Gère la modification du champ identifiant.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onUsernameChanged(value: String) = updateState { copy(username = value, usernameError = null) }
    /**
     * Rôle : Gère la modification du champ first name.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onFirstNameChanged(value: String) = updateState { copy(firstName = value, firstNameError = null) }
    /**
     * Rôle : Gère la modification du champ last name.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onLastNameChanged(value: String) = updateState { copy(lastName = value, lastNameError = null) }
    /**
     * Rôle : Gère la modification du champ email.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onEmailChanged(value: String) = updateState { copy(email = value, emailError = null) }
    /**
     * Rôle : Gère la modification du champ mot de passe.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onPasswordChanged(value: String) = updateState { copy(password = value, passwordError = null) }
    /**
     * Rôle : Gère la modification du champ téléphone.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onPhoneChanged(value: String) = updateState { copy(phone = value, phoneError = null) }

    /**
     * Rôle : Exécute l'action submit registration du module l'authentification.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun submitRegistration() {
        val currentState = _uiState.value
        val validation = AuthFormValidator.validateRegister(
            username = currentState.username,
            firstName = currentState.firstName,
            lastName = currentState.lastName,
            email = currentState.email,
            password = currentState.password,
            phone = currentState.phone,
        )
        if (validation.isInvalid) {
            _uiState.update { state ->
                state.copy(
                    usernameError = validation.usernameError,
                    firstNameError = validation.firstNameError,
                    lastNameError = validation.lastNameError,
                    emailError = validation.emailError,
                    passwordError = validation.passwordError,
                    phoneError = validation.phoneError,
                    errorMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoading = true, errorMessage = null, infoMessage = null)
            }

            authRepository.register(
                RegisterAccountInput(
                    username = currentState.username,
                    firstName = currentState.firstName,
                    lastName = currentState.lastName,
                    email = currentState.email,
                    password = currentState.password,
                    phone = currentState.phone.takeIf { it.isNotBlank() },
                ),
            ).onSuccess { message ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        infoMessage = message,
                        pendingVerificationEmail = currentState.email.trim(),
                        password = "",
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        errorMessage = throwable.localizedMessage ?: "Inscription impossible.",
                    )
                }
            }
        }
    }

    /**
     * Rôle : Consomme en attente verification email.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun consumePendingVerificationEmail() {
        _uiState.update { state ->
            state.copy(pendingVerificationEmail = null)
        }
    }

    /**
     * Rôle : Exécute l'action mise à jour état du module l'authentification.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun updateState(transform: RegisterUiState.() -> RegisterUiState) {
        _uiState.update { state ->
            state.transform().copy(errorMessage = null, infoMessage = null)
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
                    RegisterViewModel(authRepository)
                }
            }
        }
    }
}
