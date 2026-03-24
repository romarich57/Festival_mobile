package com.projetmobile.mobile.ui.screens.festival

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.projetmobile.mobile.ui.components.festival.FestivalList

/**
 * Écran liste des festivals.
 *
 * Fait le pont entre FestivalViewModel et FestivalList.
 * Seul ce fichier connaît le ViewModel.
 *
 * @param viewModel       Source de vérité.
 * @param isAuthenticated Contrôle la visibilité du FAB +.
 * @param canDelete       Suppression (isSuperOrganizer quand rôles prêts).
 * @param onFestivalClick Navigation vers ReservationDashboard.
 * @param onAddClick      Navigation vers FestivalFormScreen.
 */
@Composable
fun FestivalScreen(
    viewModel: FestivalViewModel,
    modifier: Modifier = Modifier,
    isAuthenticated: Boolean = false,
    canDelete: Boolean = false,
    onFestivalClick: (id: Int) -> Unit = {},
    onAddClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentFestivalId by viewModel.currentFestivalId.collectAsStateWithLifecycle()

    FestivalList(
        festivals = uiState.festivals,
        currentFestivalId = currentFestivalId,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        canDelete = canDelete,
        canAdd = isAuthenticated,
        modifier = modifier,
        onSelect = { id ->
            if (id != null) {
                viewModel.selectFestival(id)
                onFestivalClick(id)
            } else {
                viewModel.clearSelection()
            }
        },
        onDeleteRequest = { id -> viewModel.requestDeleteFestival(id) },
        onAddClick = onAddClick,
        onRetry = { viewModel.loadFestivals() },
    )
}