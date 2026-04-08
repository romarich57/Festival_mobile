/**
 * Rôle : Affiche l'écran d'inscription pour créer un nouveau compte utilisateur.
 * Contient un formulaire structuré et délègue l'affichage au composant `RegisterScreenLayout` pour la responsivité.
 * Précondition : Appelée par le routeur de navigation lors de l'accès à la route `Register`.
 * Postcondition : Affiche les champs de saisie (pseudo, nom, email...) et relaye les données au `RegisterViewModel`.
 */
package com.projetmobile.mobile.ui.screens.auth.register

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard

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
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val layoutMode = registerLayoutModeFor(maxHeight)
        val requiresScrollFallback = layoutMode == RegisterLayoutMode.EmergencyScroll || uiState.hasOverflowRisk
        val contentPadding = when (layoutMode) {
            RegisterLayoutMode.Standard -> 24.dp
            RegisterLayoutMode.Compact, RegisterLayoutMode.EmergencyScroll -> 20.dp
        }
        val verticalSpacing = when (layoutMode) {
            RegisterLayoutMode.Standard -> 12.dp
            RegisterLayoutMode.Compact, RegisterLayoutMode.EmergencyScroll -> 10.dp
        }

        AuthCard(modifier = Modifier.fillMaxSize()) {
            if (requiresScrollFallback) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(contentPadding),
                    verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                ) {
                    RegisterFieldsSection(
                        uiState = uiState,
                        onUsernameChanged = onUsernameChanged,
                        onFirstNameChanged = onFirstNameChanged,
                        onLastNameChanged = onLastNameChanged,
                        onEmailChanged = onEmailChanged,
                        onPasswordChanged = onPasswordChanged,
                        onPhoneChanged = onPhoneChanged,
                        verticalSpacing = verticalSpacing,
                    )
                    RegisterActionsSection(
                        uiState = uiState,
                        onSubmit = onSubmit,
                        onNavigateLogin = onNavigateLogin,
                        verticalSpacing = verticalSpacing,
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding),
                ) {
                    RegisterFieldsSection(
                        uiState = uiState,
                        onUsernameChanged = onUsernameChanged,
                        onFirstNameChanged = onFirstNameChanged,
                        onLastNameChanged = onLastNameChanged,
                        onEmailChanged = onEmailChanged,
                        onPasswordChanged = onPasswordChanged,
                        onPhoneChanged = onPhoneChanged,
                        verticalSpacing = verticalSpacing,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    RegisterActionsSection(
                        uiState = uiState,
                        onSubmit = onSubmit,
                        onNavigateLogin = onNavigateLogin,
                        verticalSpacing = verticalSpacing,
                    )
                }
            }
        }
    }
}
