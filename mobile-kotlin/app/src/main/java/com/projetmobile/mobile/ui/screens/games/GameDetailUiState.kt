package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.GameDetail

data class GameDetailUiState(
    val gameId: Int,
    val game: GameDetail? = null,
    val canManageGames: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
