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

class ForgotPasswordViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

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

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    ForgotPasswordViewModel(authRepository)
                }
            }
        }
    }
}
