package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.GameSort
import com.projetmobile.mobile.data.entity.games.PagedResult

internal interface GamesCatalogStateReducer {
    fun onTitleChanged(state: GamesCatalogUiState, value: String): GamesCatalogUiState
    fun onTypeSelected(state: GamesCatalogUiState, value: String?): GamesCatalogUiState
    fun onEditorSelected(state: GamesCatalogUiState, editorId: Int?): GamesCatalogUiState
    fun onMinAgeChanged(state: GamesCatalogUiState, value: String): GamesCatalogUiState
    fun onSortSelected(state: GamesCatalogUiState, sort: GameSort): GamesCatalogUiState
    fun onToggleVisibleColumn(state: GamesCatalogUiState, column: GameVisibleColumn): GamesCatalogUiState
    fun onRequestDelete(state: GamesCatalogUiState, game: GameListItem): GamesCatalogUiState
    fun onDismissDeleteDialog(state: GamesCatalogUiState): GamesCatalogUiState
    fun onDismissInfoMessage(state: GamesCatalogUiState): GamesCatalogUiState
    fun onDismissErrorMessage(state: GamesCatalogUiState): GamesCatalogUiState
    fun onLookupsLoaded(state: GamesCatalogUiState, result: GamesCatalogLookupsLoadResult): GamesCatalogUiState
    fun onRefreshStarted(state: GamesCatalogUiState, infoMessage: String?): GamesCatalogUiState
    fun onRefreshSucceeded(
        state: GamesCatalogUiState,
        page: PagedResult<GameListItem>,
    ): GamesCatalogUiState
    fun onRefreshFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState
    fun onLoadNextPageStarted(state: GamesCatalogUiState): GamesCatalogUiState
    fun onLoadNextPageSucceeded(
        state: GamesCatalogUiState,
        page: PagedResult<GameListItem>,
    ): GamesCatalogUiState
    fun onLoadNextPageFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState
    fun onDeleteStarted(state: GamesCatalogUiState, gameId: Int): GamesCatalogUiState
    fun onDeleteSucceeded(
        state: GamesCatalogUiState,
        game: GameListItem,
        message: String,
    ): GamesCatalogUiState
    fun onDeleteFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState
}
internal class DefaultGamesCatalogStateReducer : GamesCatalogStateReducer {
    override fun onTitleChanged(state: GamesCatalogUiState, value: String): GamesCatalogUiState {
        return state.copy(
            filters = state.filters.copy(title = value),
            errorMessage = null,
            infoMessage = null,
        )
    }
    override fun onTypeSelected(state: GamesCatalogUiState, value: String?): GamesCatalogUiState {
        return state.copy(
            filters = state.filters.copy(selectedType = value),
            errorMessage = null,
            infoMessage = null,
        )
    }
    override fun onEditorSelected(
        state: GamesCatalogUiState,
        editorId: Int?,
    ): GamesCatalogUiState {
        return state.copy(
            filters = state.filters.copy(selectedEditorId = editorId),
            errorMessage = null,
            infoMessage = null,
        )
    }
    override fun onMinAgeChanged(state: GamesCatalogUiState, value: String): GamesCatalogUiState {
        if (value.isNotEmpty() && value.any { !it.isDigit() }) {
            return state
        }
        return state.copy(
            filters = state.filters.copy(minAgeInput = value),
            errorMessage = null,
            infoMessage = null,
        )
    }
    override fun onSortSelected(state: GamesCatalogUiState, sort: GameSort): GamesCatalogUiState {
        return state.copy(
            filters = state.filters.copy(sort = sort),
            errorMessage = null,
            infoMessage = null,
        )
    }
    override fun onToggleVisibleColumn(
        state: GamesCatalogUiState,
        column: GameVisibleColumn,
    ): GamesCatalogUiState {
        val updatedColumns = if (column in state.visibleColumns) {
            state.visibleColumns - column
        } else {
            state.visibleColumns + column
        }
        return state.copy(
            visibleColumns = updatedColumns.ifEmpty { GameVisibleColumn.entries.toSet() },
        )
    }
    override fun onRequestDelete(state: GamesCatalogUiState, game: GameListItem): GamesCatalogUiState {
        return state.copy(
            pendingDeletion = game,
            errorMessage = null,
            infoMessage = null,
        )
    }
    override fun onDismissDeleteDialog(state: GamesCatalogUiState): GamesCatalogUiState {
        return state.copy(pendingDeletion = null)
    }
    override fun onDismissInfoMessage(state: GamesCatalogUiState): GamesCatalogUiState {
        return state.copy(infoMessage = null)
    }
    override fun onDismissErrorMessage(state: GamesCatalogUiState): GamesCatalogUiState {
        return state.copy(errorMessage = null)
    }
    override fun onLookupsLoaded(
        state: GamesCatalogUiState,
        result: GamesCatalogLookupsLoadResult,
    ): GamesCatalogUiState {
        val shouldShowLookupInfo = state.items.isNotEmpty() && result.errorMessage != null
        return state.copy(
            availableTypes = result.availableTypes,
            availableEditors = result.availableEditors,
            infoMessage = if (shouldShowLookupInfo) result.errorMessage else state.infoMessage,
            errorMessage = if (shouldShowLookupInfo) {
                state.errorMessage
            } else {
                result.errorMessage ?: state.errorMessage
            },
        )
    }
    override fun onRefreshStarted(
        state: GamesCatalogUiState,
        infoMessage: String?,
    ): GamesCatalogUiState {
        val hadItems = state.items.isNotEmpty()
        return state.copy(
            isLoading = !hadItems,
            isRefreshing = hadItems,
            errorMessage = null,
            infoMessage = infoMessage ?: state.infoMessage,
            pendingDeletion = null,
        )
    }
    override fun onRefreshSucceeded(
        state: GamesCatalogUiState,
        page: PagedResult<GameListItem>,
    ): GamesCatalogUiState {
        return state.copy(
            items = page.items,
            currentPage = page.page,
            total = page.total,
            hasNext = page.hasNext,
            isLoading = false,
            isRefreshing = false,
        )
    }
    override fun onRefreshFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState {
        val shouldShowOfflineInfo = state.items.isNotEmpty() && (
            message.startsWith("Mode hors-ligne") ||
                message.startsWith("Serveur inaccessible")
            )
        return state.copy(
            isLoading = false,
            isRefreshing = false,
            infoMessage = if (shouldShowOfflineInfo) message else state.infoMessage,
            errorMessage = if (shouldShowOfflineInfo) null else message,
        )
    }
    override fun onLoadNextPageStarted(state: GamesCatalogUiState): GamesCatalogUiState {
        return state.copy(
            isLoadingNextPage = true,
            errorMessage = null,
        )
    }
    override fun onLoadNextPageSucceeded(
        state: GamesCatalogUiState,
        page: PagedResult<GameListItem>,
    ): GamesCatalogUiState {
        return state.copy(
            items = state.items + page.items,
            currentPage = page.page,
            total = page.total,
            hasNext = page.hasNext,
            isLoadingNextPage = false,
        )
    }
    override fun onLoadNextPageFailed(
        state: GamesCatalogUiState,
        message: String,
    ): GamesCatalogUiState {
        return state.copy(
            isLoadingNextPage = false,
            errorMessage = message,
        )
    }
    override fun onDeleteStarted(state: GamesCatalogUiState, gameId: Int): GamesCatalogUiState {
        return state.copy(
            deletingGameId = gameId,
            errorMessage = null,
            infoMessage = null,
        )
    }
    override fun onDeleteSucceeded(
        state: GamesCatalogUiState,
        game: GameListItem,
        message: String,
    ): GamesCatalogUiState {
        return state.copy(
            items = state.items.filterNot { it.id == game.id },
            total = (state.total - 1).coerceAtLeast(0),
            deletingGameId = null,
            pendingDeletion = null,
            infoMessage = message,
        )
    }
    override fun onDeleteFailed(state: GamesCatalogUiState, message: String): GamesCatalogUiState {
        return state.copy(
            deletingGameId = null,
            errorMessage = message,
        )
    }
}
