package com.projetmobile.mobile.ui.screens.auth.forgotpassword

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import com.projetmobile.mobile.ui.components.AuthLinkButton
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
fun ForgotPasswordScreen(
    uiState: ForgotPasswordUiState,
    onEmailChanged: (String) -> Unit,
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
                text = "Saisissez l'email associé à votre compte. Vous recevrez un lien pour choisir un nouveau mot de passe (valide 1h).",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            FestivalTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = "Email",
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.emailError != null,
                supportingText = uiState.emailError,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                ),
            )

            PrimaryAuthButton(
                text = if (uiState.isLoading) {
                    "Envoi..."
                } else {
                    "Envoyer un lien de réinitialisation"
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                onClick = onSubmit,
            )

            uiState.successMessage?.let { message ->
                AuthFeedbackBanner(
                    message = message,
                    tone = AuthFeedbackTone.Success,
                )
            }

            uiState.errorMessage?.let { message ->
                AuthFeedbackBanner(
                    message = message,
                    tone = AuthFeedbackTone.Error,
                )
            }

            AuthLinkButton(
                text = "Retourner à la connexion",
                onClick = onBackToLogin,
            )
        }
    }
}
