/**
 * Rôle : Structure de navigation racine pour l'écran du formulaire de jeu.
 *
 * Précondition : Instancié par le NavGraph avec ou sans ID (création/édition).
 *
 * Postcondition : Permet à UI global de construire le composable Form et son ViewModel.
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
 * Rôle : Exécute l'action jeu formulaire route du module les jeux formulaire.
 *
 * Précondition : L'état UI et les callbacks ou dépendances nécessaires doivent être disponibles.
 *
 * Postcondition : L'interface reflète l'état courant et propage les événements utilisateur.
 */
internal fun GameFormRoute(
    gamesRepository: GamesRepository,
    mode: GameFormMode,
    onBackToList: () -> Unit,
    onGameSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: GameFormViewModel = viewModel(
        factory = gameFormViewModelFactory(
            gamesRepository = gamesRepository,
            mode = mode,
        ),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.value.completedMessage) {
        val message = uiState.value.completedMessage ?: return@LaunchedEffect
        viewModel.consumeCompletion()
        onGameSaved(message)
    }

    val actions = remember(viewModel, onBackToList) {
        GameFormActions(
            onTitleChanged = viewModel::onTitleChanged,
            onTypeChanged = viewModel::onTypeChanged,
            onSelectSuggestedType = viewModel::onSelectSuggestedType,
            onEditorSelected = viewModel::onEditorSelected,
            onMinAgeChanged = viewModel::onMinAgeChanged,
            onAuthorsChanged = viewModel::onAuthorsChanged,
            onMinPlayersChanged = viewModel::onMinPlayersChanged,
            onMaxPlayersChanged = viewModel::onMaxPlayersChanged,
            onDurationMinutesChanged = viewModel::onDurationMinutesChanged,
            onPrototypeChanged = viewModel::onPrototypeChanged,
            onThemeChanged = viewModel::onThemeChanged,
            onDescriptionChanged = viewModel::onDescriptionChanged,
            onImageUrlChanged = viewModel::onImageUrlChanged,
            onRulesVideoUrlChanged = viewModel::onRulesVideoUrlChanged,
            onToggleMechanism = viewModel::onToggleMechanism,
            onImageSourceModeChanged = viewModel::onImageSourceModeChanged,
            onLocalImageSelected = viewModel::onLocalImageSelected,
            onSaveGame = viewModel::saveGame,
            onDismissErrorMessage = viewModel::dismissErrorMessage,
            onBackToList = onBackToList,
        )
    }

    GameFormScreen(
        uiState = uiState.value,
        actions = actions,
        modifier = modifier,
    )
}
