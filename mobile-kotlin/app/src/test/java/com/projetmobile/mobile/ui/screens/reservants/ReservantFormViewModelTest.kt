package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantEditorOption
import com.projetmobile.mobile.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReservantFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun createMode_loadsEditorsAndCreatesReservant() = runTest {
        var createDraft: ReservantDraft? = null
        var editorsCalls = 0

        val viewModel = ReservantFormViewModel(
            loadEditors = {
                editorsCalls += 1
                Result.success(
                    listOf(
                        ReservantEditorOption(
                            id = 9,
                            name = "Blue Fox",
                            email = null,
                            website = null,
                            description = null,
                            logoUrl = null,
                            isExhibitor = true,
                            isDistributor = false,
                        ),
                    ),
                )
            },
            loadReservant = { Result.failure(IllegalStateException("unused")) },
            createReservant = { draft ->
                createDraft = draft
                Result.success(sampleDetail(id = 21, type = draft.type, editorId = draft.editorId))
            },
            updateReservant = { _, _ -> Result.failure(IllegalStateException("unused")) },
            mode = ReservantFormMode.Create,
            currentUserRole = "organizer",
        )

        advanceUntilIdle()

        viewModel.onNameChanged("Blue Fox")
        viewModel.onEmailChanged("contact@bluefox.test")
        viewModel.onTypeSelected(ReservantTypeChoice.Editor.value)
        viewModel.onLinkedEditorSelected(9)
        viewModel.onPhoneNumberChanged("0601020304")
        viewModel.onNotesChanged("Catalogue principal")
        viewModel.saveReservant()

        advanceUntilIdle()

        assertEquals(1, editorsCalls)
        assertEquals("editeur", createDraft?.type)
        assertEquals(9, createDraft?.editorId)
        assertEquals(21, viewModel.uiState.value.completedReservantId)
        assertTrue(viewModel.uiState.value.canManageReservants)
    }

    @Test
    fun editMode_preservesImmutableSnapshotFieldsWhenSaving() = runTest {
        var updatedDraft: ReservantDraft? = null
        var updatedId: Int? = null

        val viewModel = ReservantFormViewModel(
            loadEditors = { Result.success(emptyList()) },
            loadReservant = {
                Result.success(
                    sampleDetail(
                        id = 14,
                        type = "prestataire",
                        editorId = 33,
                        address = "12 rue des jeux",
                        siret = "12345678900011",
                        notes = "Initial",
                    ),
                )
            },
            createReservant = { Result.failure(IllegalStateException("unused")) },
            updateReservant = { reservantId, draft ->
                updatedId = reservantId
                updatedDraft = draft
                Result.success(sampleDetail(id = reservantId, type = draft.type, editorId = draft.editorId))
            },
            mode = ReservantFormMode.Edit(14),
            currentUserRole = "admin",
        )

        advanceUntilIdle()

        viewModel.onNameChanged("Prestataire mis à jour")
        viewModel.onEmailChanged("maj@bluefox.test")
        viewModel.onPhoneNumberChanged("0605060708")
        viewModel.onNotesChanged("Notes mises à jour")
        viewModel.saveReservant()

        advanceUntilIdle()

        assertEquals(14, updatedId)
        assertEquals("prestataire", updatedDraft?.type)
        assertEquals(33, updatedDraft?.editorId)
        assertEquals("12 rue des jeux", updatedDraft?.address)
        assertEquals("12345678900011", updatedDraft?.siret)
        assertEquals("Notes mises à jour", updatedDraft?.notes)
    }

    private fun sampleDetail(
        id: Int,
        type: String,
        editorId: Int?,
        address: String? = null,
        siret: String? = null,
        notes: String? = null,
    ): ReservantDetail {
        return ReservantDetail(
            id = id,
            name = "Reservant $id",
            email = "reservant$id@test.dev",
            type = type,
            editorId = editorId,
            phoneNumber = "0600000000",
            address = address,
            siret = siret,
            notes = notes,
        )
    }
}
