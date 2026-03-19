package com.projetmobile.mobile.ui.utils.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppSessionUiState(
    val isRestoring: Boolean = true,
    val isLoggingOut: Boolean = false,
    val currentUser: AuthUser? = null,
    val errorMessage: String? = null,
)

class AppSessionViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppSessionUiState())
    val uiState: StateFlow<AppSessionUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    fun restoreSession() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isRestoring = true, errorMessage = null)
            }

            authRepository.restoreSession()
                .onSuccess { user ->
                    _uiState.value = AppSessionUiState(
                        isRestoring = false,
                        currentUser = user,
                        errorMessage = null,
                    )
                }
                .onFailure { error ->
                    _uiState.value = AppSessionUiState(
                        isRestoring = false,
                        currentUser = null,
                        errorMessage = error.localizedMessage ?: "Impossible de restaurer la session.",
                    )
                }
        }
    }

    fun onUserAuthenticated(user: AuthUser) {
        _uiState.update { state ->
            state.copy(currentUser = user, isRestoring = false, errorMessage = null)
        }
    }

    fun onUserProfileUpdated(user: AuthUser) {
        _uiState.update { state ->
            state.copy(currentUser = user, errorMessage = null)
        }
    }

    fun logout() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(isLoggingOut = true, errorMessage = null)
            }

            authRepository.logout()
                .onSuccess {
                    _uiState.value = AppSessionUiState(
                        isRestoring = false,
                        isLoggingOut = false,
                        currentUser = null,
                        errorMessage = null,
                    )
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoggingOut = false,
                            errorMessage = error.localizedMessage ?: "Impossible de se déconnecter.",
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    AppSessionViewModel(authRepository)
                }
            }
        }
    }
}
