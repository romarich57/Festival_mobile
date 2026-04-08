package com.projetmobile.mobile.ui.screens.admin.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.RepositoryException
import com.projetmobile.mobile.data.repository.isOfflineFriendlyFailure
import com.projetmobile.mobile.data.repository.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class AdminUserDetailViewModel(
    private val adminRepository: AdminRepository,
    private val userId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserDetailUiState())
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    fun loadUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            adminRepository.getUserById(userId)
                .onSuccess { user ->
                    _uiState.update { it.copy(user = user, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        val repositoryException = error as? RepositoryException
                        val shouldShowOfflineInfo = it.user != null &&
                            repositoryException?.kind?.isOfflineFriendlyFailure() == true
                        it.copy(
                            isLoading = false,
                            errorMessage = if (shouldShowOfflineInfo) {
                                null
                            } else {
                                error.localizedMessage ?: "Impossible de charger l'utilisateur."
                            },
                        )
                    }
                }
        }
    }

    companion object {
        fun factory(adminRepository: AdminRepository, userId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { AdminUserDetailViewModel(adminRepository, userId) }
            }
    }
}
