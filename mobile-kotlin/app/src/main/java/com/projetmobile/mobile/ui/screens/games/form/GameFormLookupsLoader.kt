package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.repository.games.GamesRepository

internal data class GameFormLookupsLoadResult(
    val availableTypes: List<GameTypeOption> = emptyList(),
    val availableEditors: List<EditorOption> = emptyList(),
    val availableMechanisms: List<MechanismOption> = emptyList(),
    val errorMessage: String? = null,
)

internal interface GameFormLookupsLoader {
    suspend fun load(): GameFormLookupsLoadResult
}

internal class RepositoryGameFormLookupsLoader(
    private val gamesRepository: GamesRepository,
) : GameFormLookupsLoader {
    override suspend fun load(): GameFormLookupsLoadResult {
        val typesResult = gamesRepository.getGameTypes()
        val editorsResult = gamesRepository.getEditors()
        val mechanismsResult = gamesRepository.getMechanisms()

        return GameFormLookupsLoadResult(
            availableTypes = typesResult.getOrDefault(emptyList()),
            availableEditors = editorsResult.getOrDefault(emptyList()),
            availableMechanisms = mechanismsResult.getOrDefault(emptyList()),
            errorMessage = listOfNotNull(
                typesResult.exceptionOrNull()?.localizedMessage,
                editorsResult.exceptionOrNull()?.localizedMessage,
                mechanismsResult.exceptionOrNull()?.localizedMessage,
            ).firstOrNull(),
        )
    }
}
