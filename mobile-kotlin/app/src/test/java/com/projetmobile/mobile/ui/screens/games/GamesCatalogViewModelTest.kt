package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.testutils.FakeGamesRepository
import com.projetmobile.mobile.testutils.MainDispatcherRule
import com.projetmobile.mobile.testutils.sampleGameListItem
import com.projetmobile.mobile.testutils.sampleGamesPage
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.repository.RepositoryException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GamesCatalogViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_loadsLookupsAndFirstPage() = runTest {
        val repository = FakeGamesRepository(
            initialPage = sampleGamesPage(
                items = listOf(sampleGameListItem(id = 1, title = "Akropolis")),
            ),
        )

        val viewModel = viewModel(repository)
        advanceUntilIdle()

        assertEquals(1, repository.getGamesCalls)
        assertEquals(1, viewModel.uiState.value.items.size)
        assertEquals(2, viewModel.uiState.value.availableTypes.size)
        assertEquals(1, viewModel.uiState.value.availableEditors.size)
    }

    @Test
    fun onTitleChanged_debouncesRefresh() = runTest {
        val repository = FakeGamesRepository()
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.onTitleChanged("Ark")
        advanceTimeBy(349)
        assertEquals(1, repository.getGamesCalls)

        advanceTimeBy(1)
        advanceUntilIdle()

        assertEquals(2, repository.getGamesCalls)
        assertEquals("Ark", repository.lastFilters?.title)
    }

    @Test
    fun loadNextPage_appendsItems() = runTest {
        val repository = FakeGamesRepository(
            initialPage = sampleGamesPage(
                items = listOf(sampleGameListItem(id = 1, title = "Akropolis")),
                hasNext = true,
            ),
        ).apply {
            pageResults[2] = Result.success(
                sampleGamesPage(
                    items = listOf(sampleGameListItem(id = 2, title = "Harmonies")),
                    page = 2,
                    total = 2,
                    hasNext = false,
                ),
            )
        }

        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals(2, repository.getGamesCalls)
        assertEquals(2, viewModel.uiState.value.items.size)
        assertEquals("Harmonies", viewModel.uiState.value.items.last().title)
    }

    @Test
    fun confirmDelete_removesItemAndPublishesMessageOnSuccess() = runTest {
        val repository = FakeGamesRepository(
            initialPage = sampleGamesPage(
                items = listOf(sampleGameListItem(id = 8, title = "Harmonies")),
                total = 1,
            ),
        )
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        val game = viewModel.uiState.value.items.first()
        viewModel.requestDelete(game)
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals(1, repository.deleteGameCalls)
        assertTrue(viewModel.uiState.value.items.isEmpty())
        assertEquals("Jeu supprimé.", viewModel.uiState.value.infoMessage)
    }

    @Test
    fun confirmDelete_surfacesErrorOnFailure() = runTest {
        val repository = FakeGamesRepository(
            initialPage = sampleGamesPage(
                items = listOf(sampleGameListItem(id = 10, title = "Heat")),
            ),
        ).apply {
            deleteGameResult = Result.failure(IllegalStateException("Suppression impossible"))
        }
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.requestDelete(viewModel.uiState.value.items.first())
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals("Impossible de supprimer le jeu.", viewModel.uiState.value.errorMessage)
        assertEquals(1, viewModel.uiState.value.items.size)
    }

    @Test
    fun confirmDelete_refreshesCatalogWhenAnotherPageExists() = runTest {
        val repository = FakeGamesRepository(
            initialPage = sampleGamesPage(
                items = listOf(sampleGameListItem(id = 8, title = "Harmonies")),
                total = 2,
                hasNext = true,
            ),
        ).apply {
            deleteGameResult = Result.success("Jeu supprimé.")
        }
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.requestDelete(viewModel.uiState.value.items.first())
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals(2, repository.getGamesCalls)
        assertEquals("Jeu supprimé.", viewModel.uiState.value.infoMessage)
    }

    @Test
    fun refreshGames_ignoresStaleResponseWhenNewerRequestWins() = runTest {
        val repository = FakeGamesRepository().apply {
            getGamesHandler = { filters: GameFilters, page: Int, limit: Int ->
                when {
                    getGamesCalls == 1 -> {
                        delay(500)
                        Result.success(
                            sampleGamesPage(
                                items = listOf(sampleGameListItem(id = 1, title = "Akropolis")),
                                page = page,
                                limit = limit,
                            ),
                        )
                    }

                    filters.title == "Heat" -> {
                        Result.success(
                            sampleGamesPage(
                                items = listOf(sampleGameListItem(id = 2, title = "Heat")),
                                page = page,
                                limit = limit,
                            ),
                        )
                    }

                    else -> Result.success(
                        sampleGamesPage(
                            items = listOf(sampleGameListItem(id = 3, title = "Fallback")),
                            page = page,
                            limit = limit,
                        ),
                    )
                }
            }
        }

        val viewModel = viewModel(repository)
        viewModel.onTitleChanged("Heat")
        advanceTimeBy(350)
        advanceUntilIdle()

        assertEquals("Heat", viewModel.uiState.value.items.single().title)
    }

    @Test
    fun confirmDelete_mapsReservationConflictToFriendlyMessage() = runTest {
        val repository = FakeGamesRepository(
            initialPage = sampleGamesPage(
                items = listOf(sampleGameListItem(id = 11, title = "Heat")),
            ),
        ).apply {
            deleteGameResult = Result.failure(
                RepositoryException(
                    statusCode = 409,
                    message = "Impossible de supprimer ce jeu car il est utilisé dans une réservation",
                ),
            )
        }
        val viewModel = viewModel(repository)
        advanceUntilIdle()

        viewModel.requestDelete(viewModel.uiState.value.items.first())
        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals(
            "Ce jeu ne peut pas être supprimé car il est utilisé dans une réservation.",
            viewModel.uiState.value.errorMessage,
        )
    }

    private fun viewModel(repository: FakeGamesRepository): GamesCatalogViewModel {
        return GamesCatalogViewModel(
            gamesRepository = repository,
            stateReducer = DefaultGamesCatalogStateReducer(),
            lookupsLoader = RepositoryGamesCatalogLookupsLoader(repository),
            currentUserRole = "organizer",
        )
    }
}
