/**
 * Rôle : Porte l'état et la logique du module l'administration catalogue pour l'écran Compose associé.
 */

package com.projetmobile.mobile.ui.screens.admin.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.repository.RepositoryException
import com.projetmobile.mobile.data.repository.isOfflineFriendlyFailure
import com.projetmobile.mobile.data.repository.admin.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rôle : Gère l'administration et le tri de tous les utilisateurs globaux sur l'application.
 *
 * Précondition : Doit avoir les droits d'administration pour charger cet ensemble via le repository.
 *
 * Postcondition : Construit et expose un StateFlow comprenant tout le catalogue d'utilisateurs et le mode de tri.
 */
internal class AdminCatalogViewModel(
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCatalogUiState())
    val uiState: StateFlow<AdminCatalogUiState> = _uiState.asStateFlow()

    init {
        loadUsers()
    }

    /**
     * Rôle : Charge utilisateurs.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun loadUsers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            adminRepository.getUsers()
                .onSuccess { users ->
                    _uiState.update { state ->
                        state.copy(allUsers = users, isLoading = false).withAppliedFilters()
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        val repositoryException = error as? RepositoryException
                        val shouldShowOfflineInfo = it.allUsers.isNotEmpty() &&
                            repositoryException?.kind?.isOfflineFriendlyFailure() == true
                        it.copy(
                            isLoading = false,
                            infoMessage = if (shouldShowOfflineInfo) {
                                error.localizedMessage ?: "Mode hors-ligne: utilisateurs locaux affichés."
                            } else {
                                it.infoMessage
                            },
                            errorMessage = if (shouldShowOfflineInfo) {
                                null
                            } else {
                                error.localizedMessage ?: "Impossible de charger les utilisateurs."
                            },
                        )
                    }
                }
        }
    }

    /**
     * Rôle : Gère la modification du champ recherche query.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query).withAppliedFilters() }
    }

    /**
     * Rôle : Gère la sélection de role filtre.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onRoleFilterSelected(role: String?) {
        _uiState.update { it.copy(roleFilter = role).withAppliedFilters() }
    }

    /**
     * Rôle : Gère la sélection de email filtre.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onEmailFilterSelected(filter: AdminEmailFilter) {
        _uiState.update { it.copy(emailFilter = filter).withAppliedFilters() }
    }

    /**
     * Rôle : Gère la sélection de tri option.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onSortOptionSelected(option: AdminUserSortOption) {
        _uiState.update { it.copy(sortOption = option).withAppliedFilters() }
    }

    /**
     * Rôle : Inverse tri order.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun toggleSortOrder() {
        _uiState.update { it.copy(sortAscending = !it.sortAscending).withAppliedFilters() }
    }

    /**
     * Rôle : Exécute l'action réinitialisation filters du module l'administration catalogue.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun resetFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                roleFilter = null,
                emailFilter = AdminEmailFilter.All,
                sortOption = AdminUserSortOption.CreatedAtDesc,
                sortAscending = false,
            ).withAppliedFilters()
        }
    }

    /**
     * Rôle : Déclenche la demande de suppression.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun requestDelete(user: AuthUser) {
        _uiState.update { it.copy(pendingDeletion = user) }
    }

    /**
     * Rôle : Ferme suppression dialogue.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun dismissDeleteDialog() {
        _uiState.update { it.copy(pendingDeletion = null) }
    }

    /**
     * Rôle : Confirme suppression.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun confirmDelete() {
        val user = _uiState.value.pendingDeletion ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(pendingDeletion = null, deletingUserId = user.id) }
            adminRepository.deleteUser(user.id)
                .onSuccess { message ->
                    _uiState.update { state ->
                        state.copy(
                            allUsers = state.allUsers.filter { it.id != user.id },
                            deletingUserId = null,
                            infoMessage = message,
                        ).withAppliedFilters()
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            deletingUserId = null,
                            errorMessage = error.localizedMessage
                                ?: "Impossible de supprimer l'utilisateur.",
                        )
                    }
                }
        }
    }

    /**
     * Rôle : Exécute l'action mise à jour utilisateur role du module l'administration catalogue.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun updateUserRole(userId: Int, newRole: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(updatingRoleForUserId = userId) }
            adminRepository.updateUser(
                id = userId,
                input = com.projetmobile.mobile.data.entity.admin.AdminUserUpdateInput(role = newRole),
            )
                .onSuccess { updatedUser ->
                    _uiState.update { state ->
                        state.copy(
                            allUsers = state.allUsers.map { if (it.id == userId) updatedUser else it },
                            updatingRoleForUserId = null,
                        ).withAppliedFilters()
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            updatingRoleForUserId = null,
                            errorMessage = error.localizedMessage
                                ?: "Impossible de modifier le rôle.",
                        )
                    }
                }
        }
    }

    /**
     * Rôle : Ferme information message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun dismissInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    /**
     * Rôle : Ferme erreur message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun dismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Rôle : Expose un singleton de support pour le module l'administration catalogue.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module l'administration catalogue.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
        fun factory(adminRepository: AdminRepository): ViewModelProvider.Factory =
            viewModelFactory {
                initializer { AdminCatalogViewModel(adminRepository) }
            }
    }
}
