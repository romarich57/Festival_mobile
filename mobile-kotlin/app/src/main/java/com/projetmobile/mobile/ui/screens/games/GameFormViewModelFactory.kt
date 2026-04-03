package com.projetmobile.mobile.ui.screens.games

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.games.GamesRepository

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
