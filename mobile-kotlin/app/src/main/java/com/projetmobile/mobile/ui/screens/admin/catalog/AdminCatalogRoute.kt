/**
 * Rôle : Route principale pour le catalogue des utilisateurs dans l'espace administrateur.
 *
 * Précondition : L'utilisateur doit être de type "admin" et la navigation Jetpack doit router vers ce composant.
 *
 * Postcondition : Affiche l'écran de gestion du catalogue avec ses filtres et la liste des entités (ex: utilisateurs).
 */
package com.projetmobile.mobile.ui.screens.admin.catalog

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projetmobile.mobile.data.repository.admin.AdminRepository
import kotlinx.coroutines.delay

@Composable
/**
 * Rôle : Exécute l'action administration catalogue route du module l'administration catalogue.
 *
 * Précondition : L'état UI et les callbacks ou dépendances nécessaires doivent être disponibles.
 *
 * Postcondition : L'interface reflète l'état courant et propage les événements utilisateur.
 */
internal fun AdminCatalogRoute(
    adminRepository: AdminRepository,
    adminRefreshSignal: Int,
    adminFlashMessage: String?,
    onConsumeAdminFlashMessage: () -> Unit,
    onCreateUser: () -> Unit,
    onOpenUserDetail: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AdminCatalogViewModel = viewModel(
        factory = AdminCatalogViewModel.factory(adminRepository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(adminRefreshSignal) {
        if (adminRefreshSignal == 0) return@LaunchedEffect
        viewModel.loadUsers()
        onConsumeAdminFlashMessage()
    }

    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage == null) return@LaunchedEffect
        delay(3_000)
        viewModel.dismissInfoMessage()
    }

    AdminCatalogScreen(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onRoleFilterSelected = viewModel::onRoleFilterSelected,
        onEmailFilterSelected = viewModel::onEmailFilterSelected,
        onSortOptionSelected = viewModel::onSortOptionSelected,
        onToggleSortOrder = viewModel::toggleSortOrder,
        onResetFilters = viewModel::resetFilters,
        onCreateUser = onCreateUser,
        onOpenUserDetail = onOpenUserDetail,
        onRequestDelete = viewModel::requestDelete,
        onDismissDeleteDialog = viewModel::dismissDeleteDialog,
        onConfirmDelete = viewModel::confirmDelete,
        onUpdateRole = viewModel::updateUserRole,
        onDismissInfoMessage = viewModel::dismissInfoMessage,
        onDismissErrorMessage = viewModel::dismissErrorMessage,
        modifier = modifier,
    )
}
