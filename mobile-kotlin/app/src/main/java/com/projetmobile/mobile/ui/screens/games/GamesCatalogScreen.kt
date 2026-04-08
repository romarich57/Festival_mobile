/**
 * Rôle : Compose l'écran les jeux et orchestre l'affichage de l'état et des actions utilisateur.
 */

package com.projetmobile.mobile.ui.screens.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import kotlinx.coroutines.delay

/**
 * Rôle : Affiche le catalogue complet des jeux (liste paginée avec filtres et tri).
 *
 * Précondition : Le ViewModel doit fournir l'état via [GamesCatalogUiState] ainsi que les actions via [GamesCatalogActions].
 *
 * Postcondition : Affiche les jeux sous forme de liste et permet la navigation vers le détail ou l'édition d'un jeu.
 */
@Composable
internal fun GamesCatalogScreen(
    uiState: GamesCatalogUiState,
    actions: GamesCatalogActions,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage == null) {
            return@LaunchedEffect
        }
        // Les messages de succès sont temporaires et s'effacent sans interaction.
        delay(4_000)
        actions.onDismissInfoMessage()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("games-catalog-root"),
    ) {
        val windowSizeClass = gamesWindowSizeClass(maxWidth)
        val useGrid = windowSizeClass == GamesWindowSizeClass.Expanded
        val rows = remember(uiState.items, useGrid) {
            // Le catalogue adapte son découpage à la largeur disponible pour rester lisible sur tablette.
            if (useGrid) {
                uiState.items.chunked(2)
            } else {
                uiState.items.map(::listOf)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        AuthFeedbackBanner(
                            message = uiState.errorMessage,
                            tone = AuthFeedbackTone.Error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("games-error-banner"),
                        )
                    }
                }
                if (uiState.infoMessage != null) {
                    item {
                        AuthFeedbackBanner(
                            message = uiState.infoMessage,
                            tone = AuthFeedbackTone.Success,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("games-info-banner"),
                        )
                    }
                }

                item {
                    GamesCatalogHeaderCard(
                        canManageGames = uiState.canManageGames,
                        filters = uiState.filters,
                        availableTypes = uiState.availableTypes,
                        availableEditors = uiState.availableEditors,
                        visibleColumns = uiState.visibleColumns,
                        actions = actions,
                        windowSizeClass = windowSizeClass,
                    )
                }

                when {
                    // Les trois états principaux restent séparés pour éviter de mélanger chargement, vide et contenu.
                    uiState.isLoading && uiState.items.isEmpty() -> item { LoadingGamesCard() }
                    uiState.items.isEmpty() -> item { EmptyGamesCard() }
                    else -> {
                        itemsIndexed(rows) { rowIndex, rowItems ->
                            if (rowIndex >= rows.lastIndex - 1 && uiState.hasNext) {
                                // Le chargement de la page suivante est déclenché quand on approche de la fin visible.
                                LaunchedEffect(rowIndex, uiState.items.size, uiState.hasNext) {
                                    actions.onLoadNextPage()
                                }
                            }
                            if (useGrid) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    rowItems.forEach { game ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            GameCatalogCard(
                                                game = game,
                                                visibleColumns = uiState.visibleColumns,
                                                canManageGames = uiState.canManageGames,
                                                isDeleting = uiState.deletingGameId == game.id,
                                                onOpenGameDetails = actions.onOpenGameDetails,
                                                onEditGame = actions.onEditGame,
                                                onRequestDelete = actions.onRequestDelete,
                                            )
                                        }
                                    }
                                    if (rowItems.size == 1) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            } else {
                                val game = rowItems.first()
                                GameCatalogCard(
                                    game = game,
                                    visibleColumns = uiState.visibleColumns,
                                    canManageGames = uiState.canManageGames,
                                    isDeleting = uiState.deletingGameId == game.id,
                                    onOpenGameDetails = actions.onOpenGameDetails,
                                    onEditGame = actions.onEditGame,
                                    onRequestDelete = actions.onRequestDelete,
                                )
                            }
                        }
                    }
                }

                if (uiState.isLoadingNextPage) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }
        }
    }

    uiState.pendingDeletion?.let { game ->
        GamesDeleteDialog(
            pendingDeletion = game,
            onDismissDeleteDialog = actions.onDismissDeleteDialog,
            onConfirmDelete = actions.onConfirmDelete,
        )
    }
}
