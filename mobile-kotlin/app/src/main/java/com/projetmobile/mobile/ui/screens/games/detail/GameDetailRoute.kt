/**
 * Rôle : Fait le lien entre la route de détail d'un jeu, son ViewModel et l'écran Compose associé.
 * Ce fichier prépare les callbacks d'actions à partir des dépendances injectées par la navigation.
 * Précondition : L'identifiant du jeu et le repository doivent être disponibles depuis le routeur.
 * Postcondition : Le détail du jeu est affiché avec des actions prêtes à être déclenchées par l'UI.
 */
package com.projetmobile.mobile.ui.screens.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projetmobile.mobile.data.repository.games.GamesRepository

/**
 * Rôle : Décrit le composant jeu détail actions du module les jeux détail.
 */
internal data class GameDetailActions(
    val onRetry: () -> Unit,
    val onDismissErrorMessage: () -> Unit,
    val onEditGame: (Int) -> Unit,
)

/**
 * Rôle : Monte l'écran de détail d'un jeu en reliant le ViewModel, les données et les callbacks de navigation.
 * Précondition : `gamesRepository`, `gameId` et `currentUserRole` doivent être fournis par la couche de navigation.
 * Postcondition : L'écran reçoit un état observable et des actions encapsulées dans `GameDetailActions`.
 */
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
