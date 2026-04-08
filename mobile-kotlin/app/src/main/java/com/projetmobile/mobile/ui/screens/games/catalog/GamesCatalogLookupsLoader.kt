/**
 * Rôle : S'occupe d'initialiser les références communes du catalogue au chargement.
 *
 * Précondition : Le contexte du ViewModel est créé.
 *
 * Postcondition : Évite l'appel serveur de données statiques à multiples reprises.
 */
package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.repository.games.GamesRepository

/**
 * Rôle : Décrit le composant jeux catalogue lookups chargement result du module les jeux catalogue.
 */
internal data class GamesCatalogLookupsLoadResult(
    val availableTypes: List<GameTypeOption> = emptyList(),
    val availableEditors: List<EditorOption> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * Rôle : Définit le contrat du module les jeux catalogue.
 */
internal interface GamesCatalogLookupsLoader {
    /**
     * Rôle : Charge .
     *
     * Précondition : La source à transformer ou à analyser doit être fournie.
     *
     * Postcondition : Le résultat retourné est normalisé et exploitable par l'UI.
     */
    suspend fun load(): GamesCatalogLookupsLoadResult
}

/**
 * Rôle : Décrit le composant repository jeux catalogue lookups loader du module les jeux catalogue.
 */
internal class RepositoryGamesCatalogLookupsLoader(
    private val gamesRepository: GamesRepository,
) : GamesCatalogLookupsLoader {
    /**
     * Rôle : Charge .
     *
     * Précondition : La source à transformer ou à analyser doit être fournie.
     *
     * Postcondition : Le résultat retourné est normalisé et exploitable par l'UI.
     */
    override suspend fun load(): GamesCatalogLookupsLoadResult {
        val typesResult = gamesRepository.getGameTypes()
        val editorsResult = gamesRepository.getEditors()
        val hasLookupFailure = typesResult.isFailure || editorsResult.isFailure

        return GamesCatalogLookupsLoadResult(
            availableTypes = typesResult.getOrDefault(emptyList()),
            availableEditors = editorsResult.getOrDefault(emptyList()),
            errorMessage = if (hasLookupFailure) {
                "Mode hors-ligne: certaines options du catalogue sont indisponibles."
            } else {
                null
            },
        )
    }
}
