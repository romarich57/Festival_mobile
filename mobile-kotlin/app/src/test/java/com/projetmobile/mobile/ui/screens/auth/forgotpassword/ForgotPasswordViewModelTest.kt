package com.projetmobile.mobile.ui.screens.auth.forgotpassword

import com.projetmobile.mobile.testutils.FakeAuthRepository
import com.projetmobile.mobile.testutils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ForgotPasswordViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun submitPasswordResetRequest_blocksOnInvalidEmailBeforeNetworkCall() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = ForgotPasswordViewModel(repository)

        viewModel.onEmailChanged("invalid-email")
        viewModel.submitPasswordResetRequest()
        advanceUntilIdle()

        assertEquals(0, repository.requestPasswordResetCalls)
        assertNotNull(viewModel.uiState.value.emailError)
        assertNull(viewModel.uiState.value.successMessage)
    }

    @Test
    fun submitPasswordResetRequest_exposesBackendSuccessMessage() = runTest {
        val repository = FakeAuthRepository().apply {
            requestPasswordResetResult = Result.success(
                "Si un compte existe pour cet email, un lien de réinitialisation vient d’être envoyé.",
            )
        }
        val viewModel = ForgotPasswordViewModel(repository)

        viewModel.onEmailChanged(" user@example.com ")
        viewModel.submitPasswordResetRequest()
        advanceUntilIdle()

        assertEquals(1, repository.requestPasswordResetCalls)
        assertEquals("user@example.com", repository.lastRequestedResetEmail)
        assertEquals(
            "Si un compte existe pour cet email, un lien de réinitialisation vient d’être envoyé.",
            viewModel.uiState.value.successMessage,
        )
        assertTrue(!viewModel.uiState.value.isLoading)
    }
}
