/**
 * Rôle : Porte l'état et la logique du module l'administration détail pour l'écran Compose associé.
 */

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

/**
 * Rôle : Manipule la récupération d'un membre global par son ID et initie la suppression de celui-ci si demandé.
 *
 * Précondition : Construit par la factory correspondante qui injecte les variables de droits et la clé étrangère [userId].
 *
 * Postcondition : Émet l'état [uiState] et exécute les suppressions.
 */
internal class AdminUserDetailViewModel(
    private val adminRepository: AdminRepository,
    private val userId: Int,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUserDetailUiState())
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState.asStateFlow()

    init {
        loadUser()
    }

    /**
     * Rôle : Charge utilisateur.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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

    /**
     * Rôle : Expose un singleton de support pour le module l'administration détail.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module l'administration détail.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
        fun factory(adminRepository: AdminRepository, userId: Int): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { AdminUserDetailViewModel(adminRepository, userId) }
            }
    }
}
