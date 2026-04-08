/**
 * Rôle : Route d'entrée pour le formulaire utilisateur côté administrateur (création ou édition).
 *
 * Précondition : Le composant NavHost doit fournir les paramètres de navigation, tels que l'ID utilisateur (si édition).
 *
 * Postcondition : Affiche le formulaire utilisateur avec les données initialisées en s'appuyant sur un ViewModel.
 */
package com.projetmobile.mobile.ui.screens.admin.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projetmobile.mobile.data.repository.admin.AdminRepository

@Composable
/**
 * Rôle : Exécute l'action administration utilisateur formulaire route du module l'administration formulaire.
 *
 * Précondition : L'état UI et les callbacks ou dépendances nécessaires doivent être disponibles.
 *
 * Postcondition : L'interface reflète l'état courant et propage les événements utilisateur.
 */
internal fun AdminUserFormRoute(
    adminRepository: AdminRepository,
    mode: AdminUserFormMode,
    onUserSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AdminUserFormViewModel = viewModel(
        factory = AdminUserFormViewModel.factory(adminRepository, mode),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedSuccessfully) {
        if (!uiState.savedSuccessfully) return@LaunchedEffect
        val message = when (mode) {
            is AdminUserFormMode.Create -> "Utilisateur créé avec succès."
            is AdminUserFormMode.Edit -> "Utilisateur mis à jour."
        }
        onUserSaved(message)
    }

    AdminUserFormScreen(
        uiState = uiState,
        onLoginChanged = viewModel::onLoginChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onFirstNameChanged = viewModel::onFirstNameChanged,
        onLastNameChanged = viewModel::onLastNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onPhoneChanged = viewModel::onPhoneChanged,
        onRoleSelected = viewModel::onRoleSelected,
        onEmailVerifiedChanged = viewModel::onEmailVerifiedChanged,
        onSubmit = viewModel::submit,
        modifier = modifier,
    )
}
