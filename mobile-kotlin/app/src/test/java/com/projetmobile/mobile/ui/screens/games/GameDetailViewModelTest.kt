package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.testutils.FakeGamesRepository
import com.projetmobile.mobile.testutils.MainDispatcherRule
import com.projetmobile.mobile.testutils.sampleGameDetail
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_loadsGameAndExposesManageFlag() = runTest {
        val repository = FakeGamesRepository().apply {
            getGameResult = Result.success(sampleGameDetail(id = 15, title = "Heat"))
        }

        val viewModel = GameDetailViewModel(
            gamesRepository = repository,
            gameId = 15,
            currentUserRole = "organizer",
        )
        advanceUntilIdle()

        assertEquals(1, repository.getGameCalls)
        assertEquals(15, repository.lastRequestedGameId)
        assertEquals("Heat", viewModel.uiState.value.game?.title)
        assertTrue(viewModel.uiState.value.canManageGames)
    }

    @Test
    fun init_surfacesErrorOnFailure() = runTest {
        val repository = FakeGamesRepository(
            initialGame = sampleGameDetail(id = 999, title = "Autre jeu"),
        ).apply {
            getGameResult = Result.failure(IllegalStateException("Chargement impossible"))
        }

        val viewModel = GameDetailViewModel(
            gamesRepository = repository,
            gameId = 8,
            currentUserRole = "visitor",
        )
        advanceUntilIdle()

        assertEquals("Impossible de charger le jeu.", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.game)
    }

    @Test
    fun refresh_keepsCachedGameWhenNetworkFails() = runTest {
        val repository = FakeGamesRepository(
            initialGame = sampleGameDetail(id = 15, title = "Cache local"),
        ).apply {
            getGameResult = Result.failure(IllegalStateException("Hors ligne"))
        }

        val viewModel = GameDetailViewModel(
            gamesRepository = repository,
            gameId = 15,
            currentUserRole = "organizer",
        )
        advanceUntilIdle()

        assertEquals("Cache local", viewModel.uiState.value.game?.title)
        assertEquals("Impossible de charger le jeu.", viewModel.uiState.value.errorMessage)
    }
}
