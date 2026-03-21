package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.repository.games.GamesRepository

internal data class GamesCatalogLookupsLoadResult(
    val availableTypes: List<GameTypeOption> = emptyList(),
    val availableEditors: List<EditorOption> = emptyList(),
    val errorMessage: String? = null,
)

internal interface GamesCatalogLookupsLoader {
    suspend fun load(): GamesCatalogLookupsLoadResult
}

internal class RepositoryGamesCatalogLookupsLoader(
    private val gamesRepository: GamesRepository,
) : GamesCatalogLookupsLoader {
    override suspend fun load(): GamesCatalogLookupsLoadResult {
        val typesResult = gamesRepository.getGameTypes()
        val editorsResult = gamesRepository.getEditors()

        return GamesCatalogLookupsLoadResult(
            availableTypes = typesResult.getOrDefault(emptyList()),
            availableEditors = editorsResult.getOrDefault(emptyList()),
            errorMessage = listOfNotNull(
                typesResult.exceptionOrNull()?.localizedMessage,
                editorsResult.exceptionOrNull()?.localizedMessage,
            ).firstOrNull(),
        )
    }
}
