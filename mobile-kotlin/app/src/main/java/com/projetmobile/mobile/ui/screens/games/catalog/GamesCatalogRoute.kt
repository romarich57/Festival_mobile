package com.projetmobile.mobile.ui.screens.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projetmobile.mobile.data.repository.games.GamesRepository

@Composable
internal fun GamesCatalogRoute(
    gamesRepository: GamesRepository,
    currentUserRole: String?,
    gamesRefreshSignal: Int,
    gamesFlashMessage: String?,
    onConsumeGamesFlashMessage: () -> Unit,
    onCreateGame: () -> Unit,
    onOpenGameDetails: (Int) -> Unit,
    onEditGame: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GamesCatalogViewModel = viewModel(
        factory = gamesCatalogViewModelFactory(
            gamesRepository = gamesRepository,
            currentUserRole = currentUserRole,
        ),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gamesRefreshSignal) {
        if (gamesRefreshSignal == 0) {
            return@LaunchedEffect
        }
        viewModel.consumeExternalRefresh(gamesFlashMessage)
        onConsumeGamesFlashMessage()
    }

    val actions = remember(viewModel, onCreateGame, onOpenGameDetails, onEditGame) {
        GamesCatalogActions(
            onTitleChanged = viewModel::onTitleChanged,
            onTypeSelected = viewModel::onTypeSelected,
            onEditorSelected = viewModel::onEditorSelected,
            onMinAgeChanged = viewModel::onMinAgeChanged,
            onSortSelected = viewModel::onSortSelected,
            onToggleVisibleColumn = viewModel::toggleVisibleColumn,
            onRefresh = viewModel::refreshGames,
            onLoadNextPage = viewModel::loadNextPage,
            onRequestDelete = viewModel::requestDelete,
            onDismissDeleteDialog = viewModel::dismissDeleteDialog,
            onConfirmDelete = viewModel::confirmDelete,
            onDismissInfoMessage = viewModel::dismissInfoMessage,
            onDismissErrorMessage = viewModel::dismissErrorMessage,
            onCreateGame = onCreateGame,
            onOpenGameDetails = onOpenGameDetails,
            onEditGame = onEditGame,
        )
    }

    GamesCatalogScreen(
        uiState = uiState.value,
        actions = actions,
        modifier = modifier,
    )
}
