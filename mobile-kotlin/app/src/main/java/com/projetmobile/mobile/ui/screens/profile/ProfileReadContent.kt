package com.projetmobile.mobile.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.PrimaryAuthButton
import com.projetmobile.mobile.ui.components.PrimaryAuthButtonTone

private val ProfileSectionContentInset = 12.dp
private val LogoutBorderColor = Color(0xFFD14343)
private val LogoutContainerColor = Color(0xFFFFEFEF)

@Composable
internal fun ProfileOverviewCard(
    uiState: ProfileUiState,
    isLoggingOut: Boolean,
    showHorizontalActions: Boolean,
    onStartEditingField: (ProfileEditableField) -> Unit,
    onCancelEditing: () -> Unit,
    onLoginChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    val user = requireNotNull(uiState.profile)

    AuthCard(modifier = Modifier.testTag("profile-summary-card")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            HeaderRow(isRefreshing = uiState.isRefreshing)
            ProfileEditableFieldRow(
                label = "Pseudo",
                value = "@${uiState.form.login.trim()}",
                textFieldValue = uiState.form.login,
                fieldTag = "profile-login-field",
                editButtonTag = "profile-login-edit-button",
                isEditing = uiState.isFieldEditing(ProfileEditableField.Login),
                errorMessage = uiState.form.loginError,
                keyboardOptions = KeyboardOptions.Default,
                onValueChange = onLoginChanged,
                onStartEditing = { onStartEditingField(ProfileEditableField.Login) },
                badge = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        EmailStatusBadge(emailVerified = user.emailVerified)
                        ProfileRoleBadge(role = user.role.toDisplayRole())
                    }
                },
            )
            ProfileEditableFieldRow(
                label = "Prénom",
                value = uiState.form.firstName.trim(),
                textFieldValue = uiState.form.firstName,
                fieldTag = "profile-first-name-field",
                editButtonTag = "profile-first-name-edit-button",
                isEditing = uiState.isFieldEditing(ProfileEditableField.FirstName),
                errorMessage = uiState.form.firstNameError,
                keyboardOptions = KeyboardOptions.Default,
                onValueChange = onFirstNameChanged,
                onStartEditing = { onStartEditingField(ProfileEditableField.FirstName) },
            )
            ProfileEditableFieldRow(
                label = "Nom",
                value = uiState.form.lastName.trim(),
                textFieldValue = uiState.form.lastName,
                fieldTag = "profile-last-name-field",
                editButtonTag = "profile-last-name-edit-button",
                isEditing = uiState.isFieldEditing(ProfileEditableField.LastName),
                errorMessage = uiState.form.lastNameError,
                keyboardOptions = KeyboardOptions.Default,
                onValueChange = onLastNameChanged,
                onStartEditing = { onStartEditingField(ProfileEditableField.LastName) },
            )
            ProfileEditableFieldRow(
                label = "Email",
                value = uiState.form.email.trim(),
                textFieldValue = uiState.form.email,
                fieldTag = "profile-email-field",
                editButtonTag = "profile-email-edit-button",
                isEditing = uiState.isFieldEditing(ProfileEditableField.Email),
                errorMessage = uiState.form.emailError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                onValueChange = onEmailChanged,
                onStartEditing = { onStartEditingField(ProfileEditableField.Email) },
            )
            ProfileEditableFieldRow(
                label = "Téléphone",
                value = uiState.form.phone.trim().ifBlank { "Non renseigné" },
                textFieldValue = uiState.form.phone,
                fieldTag = "profile-phone-field",
                editButtonTag = "profile-phone-edit-button",
                isEditing = uiState.isFieldEditing(ProfileEditableField.Phone),
                errorMessage = uiState.form.phoneError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                onValueChange = onPhoneChanged,
                onStartEditing = { onStartEditingField(ProfileEditableField.Phone) },
            )
            if (uiState.isEditing || uiState.hasPendingChanges) {
                InlineEditActions(
                    showHorizontalActions = showHorizontalActions,
                    isSaving = uiState.isSaving,
                    canSave = uiState.hasPendingChanges && !uiState.isSaving,
                    onSaveProfile = onSaveProfile,
                    onCancelEditing = onCancelEditing,
                )
            }
            LogoutAction(
                isLoggingOut = isLoggingOut,
                onLogout = onLogout,
            )
        }
    }
}

@Composable
internal fun PasswordResetCard(
    email: String,
    isSending: Boolean,
    onSendPasswordReset: () -> Unit,
) {
    AuthCard(modifier = Modifier.testTag("profile-password-card")) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = ProfileSectionContentInset),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Réinitialisation du mot de passe",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = if (email.isBlank()) {
                        "Aucun email disponible pour envoyer un lien de réinitialisation."
                    } else {
                        "Envoyez un lien sécurisé vers $email."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PrimaryAuthButton(
                text = if (isSending) "Envoi..." else "Envoyer un lien",
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("profile-password-reset-button"),
                enabled = email.isNotBlank() && !isSending,
                onClick = onSendPasswordReset,
            )
        }
    }
}

@Composable
private fun HeaderRow(isRefreshing: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ProfileSectionContentInset, end = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Informations du compte",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        if (isRefreshing) {
            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.testTag("profile-refresh-indicator"))
        }
    }
}

@Composable
private fun InlineEditActions(
    showHorizontalActions: Boolean,
    isSaving: Boolean,
    canSave: Boolean,
    onSaveProfile: () -> Unit,
    onCancelEditing: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (showHorizontalActions) 12.dp else 8.dp),
    ) {
        PrimaryAuthButton(
            text = if (isSaving) "Enregistrement..." else "Enregistrer",
            modifier = Modifier
                .weight(1f)
                .testTag("profile-save-button"),
            enabled = canSave,
            tone = PrimaryAuthButtonTone.Accent,
            onClick = onSaveProfile,
        )
        OutlinedButton(
            onClick = onCancelEditing,
            modifier = Modifier
                .weight(1f)
                .testTag("profile-cancel-button"),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text(text = "Annuler")
        }
    }
}

@Composable
private fun LogoutAction(
    isLoggingOut: Boolean,
    onLogout: () -> Unit,
) {
    OutlinedButton(
        onClick = onLogout,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("logout-button"),
        enabled = !isLoggingOut,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, LogoutBorderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = LogoutContainerColor,
            contentColor = LogoutBorderColor,
            disabledContainerColor = LogoutContainerColor.copy(alpha = 0.55f),
            disabledContentColor = LogoutBorderColor.copy(alpha = 0.7f),
        ),
    ) {
        Text(text = if (isLoggingOut) "Déconnexion..." else "Se déconnecter")
    }
}
