package com.projetmobile.mobile.ui.screens.games

import com.projetmobile.mobile.testutils.FakeGamesRepository
import com.projetmobile.mobile.testutils.MainDispatcherRule
import com.projetmobile.mobile.testutils.sampleGameDetail
import com.projetmobile.mobile.testutils.sampleMechanismOption
import com.projetmobile.mobile.data.repository.RepositoryException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_loadsLookupsAndPrefillsEditMode() = runTest {
        val repository = FakeGamesRepository().apply {
            getGameResult = Result.success(
                sampleGameDetail(
                    id = 5,
                    title = "Ark Nova",
                    mechanisms = listOf(sampleMechanismOption(id = 3, name = "Engine")),
                ),
            )
        }

        val viewModel = viewModel(
            repository = repository,
            mode = GameFormMode.Edit(5),
        )
        advanceUntilIdle()

        assertEquals(1, repository.getGameCalls)
        assertEquals(5, repository.lastRequestedGameId)
        assertEquals("Ark Nova", viewModel.uiState.value.fields.title)
        assertEquals(2, viewModel.uiState.value.availableTypes.size)
        assertEquals(1, viewModel.uiState.value.availableMechanisms.size)
    }

    @Test
    fun saveGame_blocksWhenFormIsInvalid() = runTest {
        val repository = FakeGamesRepository()
        val viewModel = viewModel(repository = repository, mode = GameFormMode.Create)
        advanceUntilIdle()

        viewModel.saveGame()
        advanceUntilIdle()

        assertEquals(0, repository.createGameCalls)
        assertNotNull(viewModel.uiState.value.fields.titleError)
        assertNotNull(viewModel.uiState.value.fields.typeError)
    }

    @Test
    fun saveGame_stopsWhenImageUploadFails() = runTest {
        val repository = FakeGamesRepository().apply {
            uploadGameImageResult = Result.failure(IllegalStateException("Upload impossible"))
        }
        val viewModel = viewModel(repository = repository, mode = GameFormMode.Create)
        advanceUntilIdle()

        fillValidForm(viewModel)
        viewModel.onImageSourceModeChanged(GameImageSourceMode.File)
        viewModel.onLocalImageSelected(
            GameImageSelectionPayload(
                fileName = "game.png",
                mimeType = "image/png",
                bytes = byteArrayOf(1, 2, 3),
                previewUriString = "content://game.png",
            ),
        )
        viewModel.saveGame()
        advanceUntilIdle()

        assertEquals(1, repository.uploadGameImageCalls)
        assertEquals(0, repository.createGameCalls)
        assertEquals("Upload impossible", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun saveGame_setsCompletionMessageOnCreateSuccess() = runTest {
        val repository = FakeGamesRepository()
        val viewModel = viewModel(repository = repository, mode = GameFormMode.Create)
        advanceUntilIdle()

        fillValidForm(viewModel)
        viewModel.saveGame()
        advanceUntilIdle()

        assertEquals(1, repository.createGameCalls)
        assertEquals("Jeu créé.", viewModel.uiState.value.completedMessage)
    }

    @Test
    fun saveGame_setsCompletionMessageOnEditSuccess() = runTest {
        val repository = FakeGamesRepository().apply {
            getGameResult = Result.success(sampleGameDetail(id = 12, title = "Heat"))
        }
        val viewModel = viewModel(repository = repository, mode = GameFormMode.Edit(12))
        advanceUntilIdle()

        viewModel.onTitleChanged("Heat Pedal to the Metal")
        viewModel.saveGame()
        advanceUntilIdle()

        assertEquals(1, repository.updateGameCalls)
        assertEquals(12, repository.lastUpdatedGameId)
        assertEquals("Jeu mis à jour.", viewModel.uiState.value.completedMessage)
    }

    @Test
    fun saveGame_mapsDuplicateTitleToTitleFieldError() = runTest {
        val repository = FakeGamesRepository().apply {
            createGameResult = Result.failure(
                RepositoryException(
                    statusCode = 409,
                    message = "Titre déjà utilisé",
                ),
            )
        }
        val viewModel = viewModel(repository = repository, mode = GameFormMode.Create)
        advanceUntilIdle()

        fillValidForm(viewModel)
        viewModel.saveGame()
        advanceUntilIdle()

        assertEquals("Un jeu avec ce titre existe déjà.", viewModel.uiState.value.fields.titleError)
        assertEquals(null, viewModel.uiState.value.errorMessage)
    }

    @Test
    fun init_mapsMissingGameToFriendlyMessage() = runTest {
        val repository = FakeGamesRepository().apply {
            getGameResult = Result.failure(
                RepositoryException(
                    statusCode = 404,
                    message = "Jeu introuvable",
                ),
            )
        }

        val viewModel = viewModel(repository = repository, mode = GameFormMode.Edit(99))
        advanceUntilIdle()

        assertEquals(
            "Le jeu n'existe plus ou a été supprimé.",
            viewModel.uiState.value.errorMessage,
        )
    }

    @Test
    fun init_keepsLookupErrorSeparateFromBlockingError() = runTest {
        val repository = FakeGamesRepository().apply {
            gameTypesResult = Result.failure(IllegalStateException("Types indisponibles"))
            getGameResult = Result.success(sampleGameDetail(id = 5, title = "Ark Nova"))
        }

        val viewModel = viewModel(repository = repository, mode = GameFormMode.Edit(5))
        advanceUntilIdle()

        assertEquals("Types indisponibles", viewModel.uiState.value.lookupErrorMessage)
        assertEquals(null, viewModel.uiState.value.errorMessage)
        assertEquals("Ark Nova", viewModel.uiState.value.fields.title)
    }

    private fun viewModel(
        repository: FakeGamesRepository,
        mode: GameFormMode,
    ): GameFormViewModel {
        return GameFormViewModel(
            gamesRepository = repository,
            mode = mode,
            validator = DefaultGameFormValidator(),
            draftMapper = DefaultGameFormDraftMapper(),
            prefillMapper = DefaultGameFormPrefillMapper(),
            lookupsLoader = RepositoryGameFormLookupsLoader(repository),
        )
    }

    private fun fillValidForm(viewModel: GameFormViewModel) {
        viewModel.onTitleChanged("Akropolis")
        viewModel.onTypeChanged("Experts")
        viewModel.onEditorSelected(9)
        viewModel.onMinAgeChanged("12")
        viewModel.onAuthorsChanged("Designer")
        viewModel.onMinPlayersChanged("1")
        viewModel.onMaxPlayersChanged("4")
        viewModel.onDurationMinutesChanged("45")
        viewModel.onThemeChanged("Ville")
        viewModel.onDescriptionChanged("Description test")
        viewModel.onRulesVideoUrlChanged("https://www.youtube.com/watch?v=test")
        viewModel.onToggleMechanism(4)

        assertTrue(viewModel.uiState.value.fields.selectedMechanismIds.contains(4))
    }
}
