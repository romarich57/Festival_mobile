package com.projetmobile.mobile.ui.screens.auth.resetpassword

import com.projetmobile.mobile.testutils.FakeAuthRepository
import com.projetmobile.mobile.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResetPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submitPasswordReset_exposesSuccessStateOnRepositorySuccess() = runTest {
        val repository = FakeAuthRepository().apply {
            resetPasswordResult = Result.success(
                "Mot de passe mis à jour. Vous pouvez vous connecter.",
            )
        }
        val viewModel = ResetPasswordViewModel(
            authRepository = repository,
            initialToken = "valid-reset-token",
        )

        viewModel.onPasswordChanged("NewPassword123!")
        viewModel.onConfirmationChanged("NewPassword123!")
        viewModel.submitPasswordReset()
        advanceUntilIdle()

        assertEquals(1, repository.resetPasswordCalls)
        assertEquals("valid-reset-token", repository.lastResetToken)
        assertEquals("Mot de passe mis à jour. Vous pouvez vous connecter.", viewModel.uiState.value.successMessage)
        assertEquals("", viewModel.uiState.value.password)
        assertEquals("", viewModel.uiState.value.confirmation)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun submitPasswordReset_surfacesBackendErrorMessage() = runTest {
        val repository = FakeAuthRepository().apply {
            resetPasswordResult = Result.failure(IllegalStateException("Token invalide ou expiré"))
        }
        val viewModel = ResetPasswordViewModel(
            authRepository = repository,
            initialToken = "expired-reset-token",
        )

        viewModel.onPasswordChanged("NewPassword123!")
        viewModel.onConfirmationChanged("NewPassword123!")
        viewModel.submitPasswordReset()
        advanceUntilIdle()

        assertEquals(1, repository.resetPasswordCalls)
        assertEquals("Token invalide ou expiré", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.successMessage)
    }
}
