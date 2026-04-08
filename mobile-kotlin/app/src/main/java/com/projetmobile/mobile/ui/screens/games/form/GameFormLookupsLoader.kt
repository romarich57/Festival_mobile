/**
 * Rôle : S'assure de récupérer et formatter les données de référence et énumérations nécessaires.
 *
 * Précondition : Accès réseau via Repository (types, éditeurs, mécanismes).
 *
 * Postcondition : L'état des formulaires propose des menus déroulants hydratés.
 */
package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.data.entity.games.EditorOption
import com.projetmobile.mobile.data.entity.games.GameTypeOption
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.repository.games.GamesRepository

/**
 * Rôle : Décrit le composant jeu formulaire lookups chargement result du module les jeux formulaire.
 */
internal data class GameFormLookupsLoadResult(
    val availableTypes: List<GameTypeOption> = emptyList(),
    val availableEditors: List<EditorOption> = emptyList(),
    val availableMechanisms: List<MechanismOption> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * Rôle : Définit le contrat du module les jeux formulaire.
 */
internal interface GameFormLookupsLoader {
    /**
     * Rôle : Charge .
     *
     * Précondition : La source à transformer ou à analyser doit être fournie.
     *
     * Postcondition : Le résultat retourné est normalisé et exploitable par l'UI.
     */
    suspend fun load(): GameFormLookupsLoadResult
}

/**
 * Rôle : Décrit le composant repository jeu formulaire lookups loader du module les jeux formulaire.
 */
internal class RepositoryGameFormLookupsLoader(
    private val gamesRepository: GamesRepository,
) : GameFormLookupsLoader {
    /**
     * Rôle : Charge .
     *
     * Précondition : La source à transformer ou à analyser doit être fournie.
     *
     * Postcondition : Le résultat retourné est normalisé et exploitable par l'UI.
     */
    override suspend fun load(): GameFormLookupsLoadResult {
        val typesResult = gamesRepository.getGameTypes()
        val editorsResult = gamesRepository.getEditors()
        val mechanismsResult = gamesRepository.getMechanisms()
        val hasLookupFailure =
            typesResult.isFailure || editorsResult.isFailure || mechanismsResult.isFailure

        return GameFormLookupsLoadResult(
            availableTypes = typesResult.getOrDefault(emptyList()),
            availableEditors = editorsResult.getOrDefault(emptyList()),
            availableMechanisms = mechanismsResult.getOrDefault(emptyList()),
            errorMessage = if (hasLookupFailure) {
                "Mode hors-ligne: certaines options du formulaire sont indisponibles."
            } else {
                null
            },
        )
    }
}
