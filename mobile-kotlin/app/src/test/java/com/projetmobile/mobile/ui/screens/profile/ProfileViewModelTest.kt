package com.projetmobile.mobile.ui.screens.profile

import com.projetmobile.mobile.data.entity.profile.OptionalField
import com.projetmobile.mobile.data.entity.profile.ProfileUpdateResult
import com.projetmobile.mobile.testutils.FakeProfileRepository
import com.projetmobile.mobile.testutils.MainDispatcherRule
import com.projetmobile.mobile.testutils.sampleProfileUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun init_refreshesProfileAndPublishesSessionSyncUser() = runTest {
        val remoteUser = sampleProfileUser(email = "updated@example.com")
        val repository = FakeProfileRepository(initialProfile = remoteUser)
        val viewModel = ProfileViewModel(
            profileRepository = repository,
            initialUser = sampleProfileUser(),
        )

        advanceUntilIdle()

        assertEquals(1, repository.getProfileCalls)
        assertEquals("updated@example.com", viewModel.uiState.value.profile?.email)
        assertEquals("updated@example.com", viewModel.uiState.value.pendingSessionUserUpdate?.email)
    }

    @Test
    fun saveProfile_doesNothingWhenNothingChanged() = runTest {
        val user = sampleProfileUser()
        val repository = FakeProfileRepository(initialProfile = user)
        val viewModel = ProfileViewModel(
            profileRepository = repository,
            initialUser = user,
        )
        advanceUntilIdle()

        viewModel.startEditing()
        advanceUntilIdle()
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(0, repository.updateProfileCalls)
        assertFalse(viewModel.uiState.value.hasPendingChanges)
    }

    @Test
    fun saveProfile_updatesEmailAndMarksUserForNewVerification() = runTest {
        val user = sampleProfileUser()
        val updatedUser = user.copy(
            email = "new@example.com",
            emailVerified = false,
        )
        val repository = FakeProfileRepository(initialProfile = user).apply {
            updateProfileResult = Result.success(
                ProfileUpdateResult(
                    message = "Profil mis a jour. Verifiez votre nouvel email.",
                    user = updatedUser,
                    emailVerificationSent = true,
                ),
            )
        }
        val viewModel = ProfileViewModel(
            profileRepository = repository,
            initialUser = user,
        )
        advanceUntilIdle()

        viewModel.startEditing()
        viewModel.onEmailChanged(" new@example.com ")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(1, repository.updateProfileCalls)
        assertEquals("new@example.com", repository.lastUpdateInput?.email)
        assertEquals("new@example.com", viewModel.uiState.value.profile?.email)
        assertFalse(viewModel.uiState.value.profile?.emailVerified ?: true)
        assertFalse(viewModel.uiState.value.isEditing)
    }

    @Test
    fun saveProfile_sendsNullAvatarWhenRemoved() = runTest {
        val user = sampleProfileUser(avatarUrl = "/uploads/avatars/original.png")
        val repository = FakeProfileRepository(initialProfile = user)
        val viewModel = ProfileViewModel(
            profileRepository = repository,
            initialUser = user,
        )
        advanceUntilIdle()

        viewModel.startEditing()
        viewModel.removeAvatar()
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(1, repository.updateProfileCalls)
        val avatarField = repository.lastUpdateInput?.avatarUrl
        assertTrue(avatarField is OptionalField.Value)
        assertEquals(null, (avatarField as OptionalField.Value).value)
    }

    @Test
    fun saveProfile_keepsEditingStateWhenAvatarUploadFails() = runTest {
        val user = sampleProfileUser()
        val repository = FakeProfileRepository(initialProfile = user).apply {
            uploadAvatarResult = Result.failure(IllegalStateException("Upload impossible"))
        }
        val viewModel = ProfileViewModel(
            profileRepository = repository,
            initialUser = user,
        )
        advanceUntilIdle()

        viewModel.startEditing()
        viewModel.onAvatarSelected(
            AvatarSelectionPayload(
                fileName = "avatar.png",
                mimeType = "image/png",
                bytes = ByteArray(32) { 1 },
                previewUriString = "content://avatar.png",
            ),
        )
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(1, repository.uploadAvatarCalls)
        assertEquals(0, repository.updateProfileCalls)
        assertTrue(viewModel.uiState.value.isEditing)
        assertEquals("Upload impossible", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun startEditingField_onlyMarksRequestedFieldAsEditable() = runTest {
        val user = sampleProfileUser()
        val repository = FakeProfileRepository(initialProfile = user)
        val viewModel = ProfileViewModel(
            profileRepository = repository,
            initialUser = user,
        )
        advanceUntilIdle()

        viewModel.startEditingField(ProfileEditableField.Login)

        assertTrue(viewModel.uiState.value.isFieldEditing(ProfileEditableField.Login))
        assertFalse(viewModel.uiState.value.isFieldEditing(ProfileEditableField.Email))
        assertTrue(viewModel.uiState.value.isEditing)
    }

    @Test
    fun saveProfile_mapsDuplicateLoginErrorToLoginField() = runTest {
        val user = sampleProfileUser()
        val repository = FakeProfileRepository(initialProfile = user).apply {
            updateProfileResult = Result.failure(IllegalStateException("Login déjà utilisé"))
        }
        val viewModel = ProfileViewModel(
            profileRepository = repository,
            initialUser = user,
        )
        advanceUntilIdle()

        viewModel.startEditingField(ProfileEditableField.Login)
        viewModel.onLoginChanged("autre-pseudo")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(1, repository.updateProfileCalls)
        assertEquals("Ce pseudo est déjà utilisé.", viewModel.uiState.value.form.loginError)
        assertNull(viewModel.uiState.value.form.emailError)
        assertTrue(viewModel.uiState.value.isFieldEditing(ProfileEditableField.Login))
    }

    @Test
    fun saveProfile_mapsDuplicateEmailErrorToEmailField() = runTest {
        val user = sampleProfileUser()
        val repository = FakeProfileRepository(initialProfile = user).apply {
            updateProfileResult = Result.failure(IllegalStateException("Email déjà utilisé"))
        }
        val viewModel = ProfileViewModel(
            profileRepository = repository,
            initialUser = user,
        )
        advanceUntilIdle()

        viewModel.startEditingField(ProfileEditableField.Email)
        viewModel.onEmailChanged("taken@example.com")
        viewModel.saveProfile()
        advanceUntilIdle()

        assertEquals(1, repository.updateProfileCalls)
        assertEquals("Cet email est déjà utilisé.", viewModel.uiState.value.form.emailError)
        assertNull(viewModel.uiState.value.form.loginError)
        assertTrue(viewModel.uiState.value.isFieldEditing(ProfileEditableField.Email))
    }

    @Test
    fun dismissInfoMessage_clearsSuccessBannerOnly() = runTest {
        val user = sampleProfileUser()
        val updatedUser = user.copy(firstName = "Romain Junior")
        val repository = FakeProfileRepository(initialProfile = user).apply {
            updateProfileResult = Result.success(
                ProfileUpdateResult(
                    message = "Profil mis a jour.",
                    user = updatedUser,
                    emailVerificationSent = false,
                ),
            )
        }
        val viewModel = ProfileViewModel(
            profileRepository = repository,
            initialUser = user,
        )
        advanceUntilIdle()

        viewModel.startEditingField(ProfileEditableField.FirstName)
        viewModel.onFirstNameChanged("Romain Junior")
        viewModel.saveProfile()
        advanceUntilIdle()

        val pendingSessionUserUpdate = viewModel.uiState.value.pendingSessionUserUpdate
        assertEquals("Profil mis a jour.", viewModel.uiState.value.infoMessage)

        viewModel.dismissInfoMessage()

        assertNull(viewModel.uiState.value.infoMessage)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(pendingSessionUserUpdate, viewModel.uiState.value.pendingSessionUserUpdate)
        assertFalse(viewModel.uiState.value.isEditing)
    }
}
