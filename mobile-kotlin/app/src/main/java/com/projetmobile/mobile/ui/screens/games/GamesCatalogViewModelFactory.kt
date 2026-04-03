package com.projetmobile.mobile.ui.screens.games

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.games.GamesRepository

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
