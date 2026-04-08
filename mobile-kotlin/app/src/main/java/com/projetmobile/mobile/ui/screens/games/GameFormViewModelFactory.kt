/**
 * Rôle : Construit la factory de ViewModel dédiée au formulaire de jeu.
 * Ce fichier encapsule les dépendances à injecter pour l'écran de création ou d'édition d'un jeu.
 * Précondition : Le repository de jeux et le mode du formulaire doivent être connus au moment de l'instanciation.
 * Postcondition : Retourne une `ViewModelProvider.Factory` prête à produire un `GameFormViewModel` configuré.
 */
package com.projetmobile.mobile.ui.screens.games

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.games.GamesRepository

/**
 * Rôle : Crée une factory typée pour instancier le `GameFormViewModel` avec ses dépendances UI.
 * Précondition : `gamesRepository` et `mode` doivent correspondre au contexte d'écran courant.
 * Postcondition : La factory produira un ViewModel initialisé avec le validateur, les mappers et le chargeur de référentiels attendus.
 */
internal fun gameFormViewModelFactory(
    gamesRepository: GamesRepository,
    mode: GameFormMode,
): ViewModelProvider.Factory {
    return viewModelFactory {
        initializer {
            GameFormViewModel(
                gamesRepository = gamesRepository,
                mode = mode,
                validator = DefaultGameFormValidator(),
                draftMapper = DefaultGameFormDraftMapper(),
                prefillMapper = DefaultGameFormPrefillMapper(),
                lookupsLoader = RepositoryGameFormLookupsLoader(gamesRepository),
            )
        }
    }
}
