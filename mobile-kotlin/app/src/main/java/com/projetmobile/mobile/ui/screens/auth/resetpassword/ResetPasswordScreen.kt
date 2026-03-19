package com.projetmobile.mobile.ui.screens.auth.resetpassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.components.AuthLinkButton
import com.projetmobile.mobile.ui.components.FestivalPasswordField
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
fun ResetPasswordScreen(
    uiState: ResetPasswordUiState,
    onPasswordChanged: (String) -> Unit,
    onConfirmationChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Choisissez un nouveau mot de passe pour votre compte. Ce lien est sécurisé et valable pendant 1 heure.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            FestivalPasswordField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                label = "Nouveau mot de passe",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError,
                enabled = !uiState.isTokenMissing && uiState.successMessage == null,
            )

            FestivalPasswordField(
                value = uiState.confirmation,
                onValueChange = onConfirmationChanged,
                label = "Confirmation",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.confirmationError != null,
                supportingText = uiState.confirmationError,
                enabled = !uiState.isTokenMissing && uiState.successMessage == null,
            )

            if (uiState.successMessage != null) {
                AuthFeedbackBanner(
                    message = uiState.successMessage,
                    tone = AuthFeedbackTone.Success,
                )
            }

            uiState.errorMessage?.let { message ->
                AuthFeedbackBanner(
                    message = message,
                    tone = AuthFeedbackTone.Error,
                )
            }

            PrimaryAuthButton(
                text = if (uiState.successMessage != null) {
                    "Retourner à la connexion"
                } else if (uiState.isLoading) {
                    "Mise à jour..."
                } else {
                    "Mettre à jour mon mot de passe"
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = if (uiState.successMessage != null) {
                    true
                } else {
                    uiState.isSubmitEnabled
                },
                onClick = if (uiState.successMessage != null) {
                    onBackToLogin
                } else {
                    onSubmit
                },
            )

            AuthLinkButton(
                text = "Retourner à la connexion",
                onClick = onBackToLogin,
            )
        }
    }
}
