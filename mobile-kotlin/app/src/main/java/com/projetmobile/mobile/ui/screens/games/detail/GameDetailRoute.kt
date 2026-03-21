package com.projetmobile.mobile.ui.screens.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projetmobile.mobile.data.repository.games.GamesRepository

internal data class GameDetailActions(
    val onRetry: () -> Unit,
    val onDismissErrorMessage: () -> Unit,
    val onEditGame: (Int) -> Unit,
)

@Composable
internal fun GameDetailRoute(
    gamesRepository: GamesRepository,
    gameId: Int,
    currentUserRole: String?,
    onEditGame: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GameDetailViewModel = viewModel(
        factory = gameDetailViewModelFactory(
            gamesRepository = gamesRepository,
            gameId = gameId,
            currentUserRole = currentUserRole,
        ),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    val actions = remember(viewModel, onEditGame) {
        GameDetailActions(
            onRetry = viewModel::refreshGame,
            onDismissErrorMessage = viewModel::dismissErrorMessage,
            onEditGame = onEditGame,
        )
    }

    GameDetailScreen(
        uiState = uiState.value,
        actions = actions,
        modifier = modifier,
    )
}
