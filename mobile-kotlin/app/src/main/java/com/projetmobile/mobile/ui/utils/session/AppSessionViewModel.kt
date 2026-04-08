/**
 * Rôle : Gère l'état global et persistant de la session d'un utilisateur (connecté ou non) au niveau de l'application.
 * Agit en coordonnateur entre les processus d'authentification (`AuthRepository`) et la UI racine.
 * Précondition : Doit être partagé/injecté au plus haut niveau de l'arborescence Compose (généralement `FestivalApp`).
 * Postcondition : Informe toute la navigation de l'état d'authentification actuel (restauration en cours, déconnecté, identifié).
 */
package com.projetmobile.mobile.ui.utils.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.data.repository.auth.AuthRepository
import com.projetmobile.mobile.data.sync.RepositorySyncScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rôle : Décrit l'état immuable du module session.
 */
data class AppSessionUiState(
    val isRestoring: Boolean = true,
    val isLoggingOut: Boolean = false,
    val currentUser: AuthUser? = null,
    val errorMessage: String? = null,
)

/**
 * Rôle : Porte l'état et la logique du module session.
 */
class AppSessionViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AppSessionUiState())
    val uiState: StateFlow<AppSessionUiState> = _uiState.asStateFlow()

    init {
        restoreSession()
    }

    /**
     * Rôle : Exécute l'action restore session du module session.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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
                    if (user != null) {
                        RepositorySyncScheduler.schedulePendingSyncAsync()
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isRestoring = false,
                            errorMessage = error.localizedMessage ?: "Impossible de restaurer la session.",
                        )
                    }
                }
        }
    }

    /**
     * Rôle : Exécute l'action on utilisateur authenticated du module session.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onUserAuthenticated(user: AuthUser) {
        _uiState.update { state ->
            state.copy(currentUser = user, isRestoring = false, errorMessage = null)
        }
        RepositorySyncScheduler.schedulePendingSyncAsync()
    }

    /**
     * Rôle : Gère la mise à jour de utilisateur profil.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun onUserProfileUpdated(user: AuthUser) {
        _uiState.update { state ->
            state.copy(currentUser = user, errorMessage = null)
        }
    }

    /**
     * Rôle : Exécute l'action logout du module session.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
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

    /**
     * Rôle : Expose un singleton de support pour le module session.
     */
    companion object {
        /**
         * Rôle : Exécute l'action factory du module session.
         *
         * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
         *
         * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
         */
        fun factory(authRepository: AuthRepository): ViewModelProvider.Factory {
            return viewModelFactory {
                initializer {
                    AppSessionViewModel(authRepository)
                }
            }
        }
    }
}
