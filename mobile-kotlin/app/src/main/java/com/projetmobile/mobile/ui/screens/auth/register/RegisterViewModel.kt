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

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onUsernameChanged(value: String) = updateState { copy(username = value, usernameError = null) }
    fun onFirstNameChanged(value: String) = updateState { copy(firstName = value, firstNameError = null) }
    fun onLastNameChanged(value: String) = updateState { copy(lastName = value, lastNameError = null) }
    fun onEmailChanged(value: String) = updateState { copy(email = value, emailError = null) }
    fun onPasswordChanged(value: String) = updateState { copy(password = value, passwordError = null) }
    fun onPhoneChanged(value: String) = updateState { copy(phone = value, phoneError = null) }

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

    fun consumePendingVerificationEmail() {
        _uiState.update { state ->
            state.copy(pendingVerificationEmail = null)
        }
    }

    private fun updateState(transform: RegisterUiState.() -> RegisterUiState) {
        _uiState.update { state ->
            state.transform().copy(errorMessage = null, infoMessage = null)
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    RegisterViewModel(authRepository)
                }
            }
        }
    }
}
