/**
 * Rôle : Porte l'état et la logique du module les jeux détail pour l'écran Compose associé.
 */

package com.projetmobile.mobile.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.entity.games.canManageGames
import com.projetmobile.mobile.data.repository.games.GamesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Rôle : Gère le chargement asynchrone des informations détaillées d'un jeu spécifique.
 *
 * Précondition : Un [gameId] de jeu est obligatoire pour formuler la requête de fetch détaillée.
 *
 * Postcondition : Informe [GameDetailUiState] de la complétion et gère les rechargements pour affichage.
 */
internal class GameDetailViewModel(
    private val gamesRepository: GamesRepository,
    private val gameId: Int,
    currentUserRole: String?,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        GameDetailUiState(
            gameId = gameId,
            canManageGames = canManageGames(currentUserRole),
        ),
    )
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    init {
        observeLocalGame()
        refreshGame()
    }

    /**
     * Rôle : Exécute l'action observe local jeu du module les jeux détail.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    private fun observeLocalGame() {
        viewModelScope.launch {
            gamesRepository.observeGame(gameId).collectLatest { localGame ->
                if (localGame == null) {
                    return@collectLatest
                }
                _uiState.update { state ->
                    state.copy(
                        game = localGame,
                        isLoading = false,
                    )
                }
            }
        }
    }

    /**
     * Rôle : Rafraîchit jeu.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun refreshGame() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = state.game == null,
                    errorMessage = null,
                )
            }
            gamesRepository.getGame(gameId)
                .onSuccess { game ->
                    _uiState.update { state ->
                        state.copy(
                            game = game,
                            isLoading = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = mapGameDetailError(error),
                        )
                    }
                }
        }
    }

    /**
     * Rôle : Ferme erreur message.
     *
     * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
     *
     * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
     */
    fun dismissErrorMessage() {
        _uiState.update { state -> state.copy(errorMessage = null) }
    }
}

/**
 * Rôle : Exécute l'action jeu détail vue modèle factory du module les jeux détail.
 *
 * Précondition : Les dépendances injectées et l'état courant du ViewModel doivent être disponibles.
 *
 * Postcondition : L'état exposé par le ViewModel est mis à jour ou l'action métier est déclenchée.
 */
internal fun gameDetailViewModelFactory(
    gamesRepository: GamesRepository,
    gameId: Int,
    currentUserRole: String?,
): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameDetailViewModel(
                gamesRepository = gamesRepository,
                gameId = gameId,
                currentUserRole = currentUserRole,
            ) as T
        }
    }
}
