package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameSort
import com.projetmobile.mobile.data.entity.games.GameTypeOption

enum class GameVisibleColumn(val label: String) {
    Type("Type"),
    Editor("Éditeur"),
    Age("Âge"),
    Players("Joueurs"),
    Authors("Auteurs"),
    Mechanisms("Mécanismes"),
}

data class GameCatalogFilterState(
    val title: String = "",
    val selectedType: String? = null,
    val selectedEditorId: Int? = null,
    val minAgeInput: String = "",
    val sort: GameSort = GameSort.TitleAsc,
) {
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
