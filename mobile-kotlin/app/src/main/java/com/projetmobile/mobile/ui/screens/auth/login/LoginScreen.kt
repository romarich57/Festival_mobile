package com.projetmobile.mobile.ui.screens.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.data.entity.auth.AuthUser
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.components.InlineAuthLinkButton
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    sessionUser: AuthUser?,
    onIdentifierChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateRegister: () -> Unit,
    onResendVerification: () -> Unit,
    onNavigateForgotPassword: () -> Unit,
    onNavigateFestivals: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    AuthCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (sessionUser != null) {
                Text(
                    text = "Vous êtes déjà connecté en tant que ${sessionUser.firstName}.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                PrimaryAuthButton(
                    text = "Aller aux festivals",
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateFestivals,
                )
                return@Column
            }

            FestivalTextField(
                value = uiState.identifier,
                onValueChange = onIdentifierChanged,
                label = "Email ou pseudo",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login-identifier-field"),
                isError = uiState.identifierError != null,
                supportingText = uiState.identifierError,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                ),
            )

            FestivalTextField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                label = "Mot de passe",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login-password-field"),
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
            )

            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }

            uiState.infoMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.secondary)
            }

            PrimaryAuthButton(
                text = if (uiState.isLoading) "Connexion..." else "Se connecter",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login-submit-button"),
                enabled = !uiState.isLoading,
                onClick = onSubmit,
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            LoginFooterLinkRow(
                prompt = "Pas encore de compte ?",
                action = "Créer un compte",
                enabled = !uiState.isLoading,
                onClick = onNavigateRegister,
            )
            LoginFooterLinkRow(
                prompt = "Email de vérification pas reçu ?",
                action = "Renvoyer un email",
                enabled = !uiState.isLoading,
                onClick = onResendVerification,
            )
            LoginFooterLinkRow(
                prompt = "Mot de passe oublié ?",
                action = "Réinitialiser",
                enabled = !uiState.isLoading,
                onClick = onNavigateForgotPassword,
            )
        }
    }
}

@Composable
private fun LoginFooterLinkRow(
    prompt: String,
    action: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        InlineAuthLinkButton(
            text = action,
            enabled = enabled,
            onClick = onClick,
        )
    }
}
