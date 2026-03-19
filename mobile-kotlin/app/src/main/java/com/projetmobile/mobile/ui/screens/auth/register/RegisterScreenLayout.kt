package com.projetmobile.mobile.ui.screens.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.FestivalPasswordField
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.components.InlineAuthLinkButton
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

internal enum class RegisterLayoutMode {
    Standard,
    Compact,
    EmergencyScroll,
}

@Composable
internal fun RegisterFieldsSection(
    uiState: RegisterUiState,
    onUsernameChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    verticalSpacing: Dp,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FestivalTextField(
                value = uiState.firstName,
                onValueChange = onFirstNameChanged,
                label = "Prénom",
                modifier = Modifier
                    .weight(1f)
                    .testTag("register-first-name-field"),
                isError = uiState.firstNameError != null,
                supportingText = uiState.firstNameError,
            )
            FestivalTextField(
                value = uiState.lastName,
                onValueChange = onLastNameChanged,
                label = "Nom",
                modifier = Modifier
                    .weight(1f)
                    .testTag("register-last-name-field"),
                isError = uiState.lastNameError != null,
                supportingText = uiState.lastNameError,
            )
        }
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
        FestivalPasswordField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = "Mot de passe",
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register-password-field"),
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError,
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
    }
}

@Composable
internal fun RegisterActionsSection(
    uiState: RegisterUiState,
    onSubmit: () -> Unit,
    onNavigateLogin: () -> Unit,
    verticalSpacing: Dp,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.error,
            )
        }
        uiState.infoMessage?.let { message ->
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.secondary,
            )
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
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Déjà inscrit ?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(6.dp))
            InlineAuthLinkButton(
                text = "Retour à la connexion",
                onClick = onNavigateLogin,
            )
        }
    }
}

internal fun registerLayoutModeFor(maxHeight: Dp): RegisterLayoutMode {
    return when {
        maxHeight >= 660.dp -> RegisterLayoutMode.Standard
        maxHeight >= 560.dp -> RegisterLayoutMode.Compact
        else -> RegisterLayoutMode.EmergencyScroll
    }
}

internal val RegisterUiState.hasOverflowRisk: Boolean
    get() = usernameError != null ||
        firstNameError != null ||
        lastNameError != null ||
        emailError != null ||
        passwordError != null ||
        phoneError != null ||
        errorMessage != null ||
        infoMessage != null
