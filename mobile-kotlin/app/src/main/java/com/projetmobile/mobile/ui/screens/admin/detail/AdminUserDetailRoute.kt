/**
 * Rôle : Route d'entrée pour la vue détaillée d'un utilisateur dans la console d'administration.
 *
 * Précondition : La navigation doit fournir l'identifiant de l'utilisateur pour charger les détails.
 *
 * Postcondition : Affiche les informations complètes de l'utilisateur sélectionné ou un écran de chargement.
 */
package com.projetmobile.mobile.ui.screens.admin.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projetmobile.mobile.data.repository.admin.AdminRepository

@Composable
/**
 * Rôle : Exécute l'action administration utilisateur détail route du module l'administration détail.
 *
 * Précondition : L'état UI et les callbacks ou dépendances nécessaires doivent être disponibles.
 *
 * Postcondition : L'interface reflète l'état courant et propage les événements utilisateur.
 */
internal fun AdminUserDetailRoute(
    adminRepository: AdminRepository,
    userId: Int,
    onEditUser: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AdminUserDetailViewModel = viewModel(
        factory = AdminUserDetailViewModel.factory(adminRepository, userId),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminUserDetailScreen(
        uiState = uiState,
        onEditUser = { uiState.user?.let { onEditUser(it.id) } },
        modifier = modifier,
    )
}
