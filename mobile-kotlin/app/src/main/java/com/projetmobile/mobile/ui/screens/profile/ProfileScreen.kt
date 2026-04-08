package com.projetmobile.mobile.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import kotlinx.coroutines.delay

/**
 * Rôle : Rend la vue de profil de la session en cours et initie les modals de mise à jour.
 *
 * Précondition : Un utilisateur logué [ProfileUiState] est indispensable.
 *
 * Postcondition : Affiche l'Avatar, ou le pseudo couramment enregistré, et gère les feedbacks temporaires du viewModel.
 */
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    isLoggingOut: Boolean,
    onStartEditingField: (ProfileEditableField) -> Unit,
    onCancelEditing: () -> Unit,
    onLoginChanged: (String) -> Unit,
    onFirstNameChanged: (String) -> Unit,
    onLastNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onDismissInfoMessage: () -> Unit,
    onSendPasswordReset: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage == null) {
            return@LaunchedEffect
        }
        delay(4_000)
        onDismissInfoMessage()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile-screen-root"),
    ) {
        val showHorizontalActions = maxWidth >= 360.dp

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (uiState.errorMessage != null) {
                item {
                    AuthFeedbackBanner(
                        message = uiState.errorMessage,
                        tone = AuthFeedbackTone.Error,
                    )
                }
            }
            if (uiState.infoMessage != null) {
                item {
                    AuthFeedbackBanner(
                        message = uiState.infoMessage,
                        tone = AuthFeedbackTone.Success,
                    )
                }
            }

            item {
                when {
                    uiState.isLoading && uiState.profile == null -> LoadingProfileCard()
                    uiState.profile != null -> ProfileOverviewCard(
                        uiState = uiState,
                        isLoggingOut = isLoggingOut,
                        showHorizontalActions = showHorizontalActions,
                        onStartEditingField = onStartEditingField,
                        onCancelEditing = onCancelEditing,
                        onLoginChanged = onLoginChanged,
                        onFirstNameChanged = onFirstNameChanged,
                        onLastNameChanged = onLastNameChanged,
                        onEmailChanged = onEmailChanged,
                        onPhoneChanged = onPhoneChanged,
                        onSaveProfile = onSaveProfile,
                        onLogout = onLogout,
                    )

                    else -> EmptyProfileCard()
                }
            }

            item {
                if (uiState.profile != null) {
                    PasswordResetCard(
                        email = uiState.profile.email,
                        isSending = uiState.isSendingPasswordReset,
                        onSendPasswordReset = onSendPasswordReset,
                    )
                }
            }
        }
    }
}
