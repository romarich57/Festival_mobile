package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.entity.reservants.ReservantContact
import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReservantDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_loadsReservantContactsAndGames() = runTest {
        var requestedEditorId: Int? = null

        val viewModel = ReservantDetailViewModel(
            reservantId = 5,
            loadReservant = { reservantId ->
                Result.success(
                    ReservantDetail(
                        id = reservantId,
                        name = "Blue Fox",
                        email = "contact@bluefox.test",
                        type = "editeur",
                        editorId = 12,
                        phoneNumber = "0601020304",
                        address = null,
                        siret = null,
                        notes = null,
                    ),
                )
            },
            loadContacts = {
                Result.success(
                    listOf(
                        ReservantContact(
                            id = 1,
                            name = "Lina",
                            email = "lina@bluefox.test",
                            phoneNumber = "0600000001",
                            jobTitle = "Commerciale",
                            priority = 1,
                        ),
                    ),
                )
            },
            addContact = { _, _ -> Result.failure(IllegalStateException("unused")) },
            loadGames = { editorId ->
                requestedEditorId = editorId
                Result.success(listOf(sampleGame(id = 33, title = "Zen Garden")))
            },
            currentUserRole = "organizer",
        )

        advanceUntilIdle()

        assertEquals("Blue Fox", viewModel.uiState.value.reservant?.name)
        assertEquals(1, viewModel.uiState.value.contacts.size)
        assertEquals(33, viewModel.uiState.value.games.single().id)
        assertEquals(12, requestedEditorId)
        assertTrue(viewModel.uiState.value.canManageReservants)
    }

    @Test
    fun saveContact_addsContactAndCollapsesForm() = runTest {
        var capturedDraft: ReservantContactDraft? = null

        val viewModel = ReservantDetailViewModel(
            reservantId = 9,
            loadReservant = { reservantId ->
                Result.success(
                    ReservantDetail(
                        id = reservantId,
                        name = "Meeple Corp",
                        email = "hello@meeple.test",
                        type = "prestataire",
                        editorId = null,
                        phoneNumber = null,
                        address = null,
                        siret = null,
                        notes = null,
                    ),
                )
            },
            loadContacts = { Result.success(emptyList()) },
            addContact = { _, draft ->
                capturedDraft = draft
                Result.success(
                    ReservantContact(
                        id = 44,
                        name = draft.name,
                        email = draft.email,
                        phoneNumber = draft.phoneNumber,
                        jobTitle = draft.jobTitle,
                        priority = draft.priority,
                    ),
                )
            },
            loadGames = { Result.success(emptyList()) },
            currentUserRole = "admin",
        )

        advanceUntilIdle()

        viewModel.toggleContactForm()
        viewModel.onContactNameChanged("Nina")
        viewModel.onContactEmailChanged("nina@meeple.test")
        viewModel.onContactPhoneNumberChanged("0609090909")
        viewModel.onContactJobTitleChanged("Support")
        viewModel.onContactPrioritySelected(1)
        viewModel.saveContact()

        advanceUntilIdle()

        assertEquals("Nina", capturedDraft?.name)
        assertEquals(1, viewModel.uiState.value.contacts.size)
        assertTrue(!viewModel.uiState.value.isContactFormExpanded)
        assertEquals("Contact ajouté.", viewModel.uiState.value.infoMessage)
    }

    private fun sampleGame(
        id: Int,
        title: String,
    ): GameListItem {
        return GameListItem(
            id = id,
            title = title,
            type = "Famille",
            editorId = 12,
            editorName = "Blue Fox",
            minAge = 8,
            authors = "Auteur",
            minPlayers = 2,
            maxPlayers = 4,
            prototype = false,
            durationMinutes = 30,
            theme = null,
            description = null,
            imageUrl = null,
            rulesVideoUrl = null,
            mechanisms = listOf(MechanismOption(id = 1, name = "Draft", description = null)),
        )
    }
}
