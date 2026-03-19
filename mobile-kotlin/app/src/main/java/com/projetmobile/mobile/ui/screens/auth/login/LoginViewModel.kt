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

    fun onPasswordChanged(value: String) {
        _uiState.update { state ->
            state.copy(
                password = value,
                passwordError = null,
                errorMessage = null,
            )
        }
    }

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

    fun consumeAuthenticatedUser() {
        _uiState.update { state ->
            state.copy(authenticatedUser = null, password = "", isLoading = false)
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    LoginViewModel(authRepository)
                }
            }
        }
    }
}
