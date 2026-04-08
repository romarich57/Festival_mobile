/**
 * Rôle : Fait le lien entre la route du catalogue de jeux, son ViewModel et l'écran Compose.
 * Ce fichier prépare les callbacks de navigation et de rafraîchissement autour du catalogue.
 * Précondition : Les dépendances du catalogue doivent être injectées par la navigation parent.
 * Postcondition : Le catalogue de jeux est affiché avec des actions prêtes à l'emploi.
 */
package com.projetmobile.mobile.ui.screens.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projetmobile.mobile.data.repository.games.GamesRepository

@Composable
/**
 * Rôle : Monte l'écran de catalogue des jeux avec son état observable et ses callbacks.
 * Précondition : `gamesRepository` et `currentUserRole` doivent être disponibles au moment du routage.
 * Postcondition : L'écran reçoit l'état du ViewModel et peut déclencher création, détail, édition et rafraîchissement.
 */
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
