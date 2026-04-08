/**
 * Rôle : Factory chargeant de créer et instancier correctement le ViewModel du catalogue.
 *
 * Précondition : Les repositories de jeux de l'application en accès.
 *
 * Postcondition : Création normalisée du ViewModel injecté dans l'écran.
 */
package com.projetmobile.mobile.ui.screens.games

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.games.GamesRepository

/**
 * Rôle : Exécute l'action jeux catalogue vue modèle factory du module les jeux.
 *
 * Précondition : L'état UI et les callbacks ou dépendances nécessaires doivent être disponibles.
 *
 * Postcondition : Une instance correctement configurée est retournée.
 */
internal fun gamesCatalogViewModelFactory(
    gamesRepository: GamesRepository,
    currentUserRole: String?,
): ViewModelProvider.Factory {
    return viewModelFactory {
        initializer {
            GamesCatalogViewModel(
                gamesRepository = gamesRepository,
                stateReducer = DefaultGamesCatalogStateReducer(),
                lookupsLoader = RepositoryGamesCatalogLookupsLoader(gamesRepository),
                currentUserRole = currentUserRole,
            )
        }
    }
}
