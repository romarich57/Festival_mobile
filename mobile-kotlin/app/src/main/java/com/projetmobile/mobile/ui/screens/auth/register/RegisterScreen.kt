package com.projetmobile.mobile.ui.screens.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.AuthLinkButton
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
fun RegisterScreen(
    uiState: RegisterUiState,
    onUsernameChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AuthCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Créez votre compte avec les informations demandées. Un email de vérification vous sera envoyé automatiquement.",
                style = MaterialTheme.typography.bodyLarge,
            )

            FestivalTextField(
                value = uiState.username,
                onValueChange = onUsernameChanged,
                label = "Pseudo",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register-username-field"),
                isError = uiState.usernameError != null,
                supportingText = uiState.usernameError,
            )
            FestivalTextField(
                value = uiState.firstName,
                onValueChange = onFirstNameChanged,
                label = "Prénom",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register-first-name-field"),
                isError = uiState.firstNameError != null,
                supportingText = uiState.firstNameError,
            )
            FestivalTextField(
                value = uiState.lastName,
                onValueChange = onLastNameChanged,
                label = "Nom",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register-last-name-field"),
                isError = uiState.lastNameError != null,
                supportingText = uiState.lastNameError,
            )
            FestivalTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                label = "Email",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register-email-field"),
                isError = uiState.emailError != null,
                supportingText = uiState.emailError,
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
                    .testTag("register-password-field"),
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                ),
            )
            FestivalTextField(
                value = uiState.phone,
                onValueChange = onPhoneChanged,
                label = "Téléphone (optionnel)",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register-phone-field"),
                isError = uiState.phoneError != null,
                supportingText = uiState.phoneError,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                ),
            )

            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
            uiState.infoMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.secondary)
            }

            PrimaryAuthButton(
                text = if (uiState.isLoading) "Création..." else "Créer mon compte",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("register-submit-button"),
                enabled = !uiState.isLoading,
                onClick = onSubmit,
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            Text(
                text = "Déjà inscrit ?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            AuthLinkButton(
                text = "Retour à la connexion",
                onClick = onNavigateLogin,
            )
        }
    }
}
