/**
 * Rôle : Décrit l'état UI immuable du module les jeux.
 */

package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameSort
import com.projetmobile.mobile.data.entity.games.GameTypeOption

/**
 * Rôle : Décrit le composant jeu visible column du module les jeux.
 */
enum class GameVisibleColumn(val label: String) {
    Type("Type"),
    Editor("Éditeur"),
    Age("Âge"),
    Players("Joueurs"),
    Authors("Auteurs"),
    Mechanisms("Mécanismes"),
}

/**
 * Rôle : Décrit l'état immuable du module les jeux.
 */
data class GameCatalogFilterState(
    val title: String = "",
    val selectedType: String? = null,
    val selectedEditorId: Int? = null,
    val minAgeInput: String = "",
    val sort: GameSort = GameSort.TitleAsc,
) {
    /**
     * Rôle : Exécute l'action to filters du module les jeux.
     *
     * Précondition : Les données du module doivent être disponibles pour initialiser ou exposer l'état.
     *
     * Postcondition : L'objet retourné décrit un état cohérent et immuable.
     */
    fun toFilters(): GameFilters {
        return GameFilters(
            title = title,
            type = selectedType,
            editorId = selectedEditorId,
            minAge = minAgeInput.toIntOrNull(),
            sort = sort,
        )
    }
}

/**
 * Rôle : Représente l'état complet du catalogue des jeux (filtres, données paginées, colonnes visibles).
 *
 * Précondition : Utilisé par GamesCatalogViewModel pour notifier GamesCatalogScreen.
 *
 * Postcondition : Fournit de immuables données (liste, chargement, options de filtres) nécessaires à l'UI.
 */
data class GamesCatalogUiState(
    val filters: GameCatalogFilterState = GameCatalogFilterState(),
    val visibleColumns: Set<GameVisibleColumn> = GameVisibleColumn.entries.toSet(),
    val availableTypes: List<GameTypeOption> = emptyList(),
    val availableEditors: List<EditorOption> = emptyList(),
    val items: List<GameListItem> = emptyList(),
    val currentPage: Int = 1,
    val pageSize: Int = 20,
    val total: Int = 0,
    val hasNext: Boolean = false,
    val canManageGames: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingNextPage: Boolean = false,
    val deletingGameId: Int? = null,
    val pendingDeletion: GameListItem? = null,
    val infoMessage: String? = null,
    val errorMessage: String? = null,
)
