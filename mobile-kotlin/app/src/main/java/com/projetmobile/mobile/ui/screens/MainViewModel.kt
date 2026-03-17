package com.projetmobile.mobile.ui.screens

import com.projetmobile.mobile.data.remote.ApiRepository

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = false,
    val message: String = "Prêt à tester la connexion API.",
    val isError: Boolean = false,
)

class MainViewModel(
    private val repository: ApiRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun checkHealth() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false)

            val result = repository.checkHealth()
            result.onSuccess { response ->
                _uiState.value = MainUiState(
                    isLoading = false,
                    isError = false,
                    message = "Backend OK: ${response.status} (${response.service})\n${response.timestamp}",
                )
            }.onFailure { throwable ->
                _uiState.value = MainUiState(
                    isLoading = false,
                    isError = true,
                    message = "Erreur API: ${throwable.message ?: "erreur inconnue"}",
                )
            }
        }
    }
}
