package com.projetmobile.mobile.ui.screens.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.entity.games.canManageGames
import com.projetmobile.mobile.data.repository.games.GamesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
        refreshGame()
    }

    fun refreshGame() {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    isLoading = true,
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
                            game = null,
                            isLoading = false,
                            errorMessage = mapGameDetailError(error),
                        )
                    }
                }
        }
    }

    fun dismissErrorMessage() {
        _uiState.update { state -> state.copy(errorMessage = null) }
    }
}

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
