/**
 * Rôle : Relie la route du catalogue des réservants à son ViewModel et à l'écran Compose.
 * Ce fichier prépare les callbacks de navigation, de chargement et de suppression autour du catalogue.
 * Précondition : Les dépendances du catalogue doivent être injectées par la navigation parente.
 * Postcondition : L'écran de catalogue reçoit un état observable et des actions prêtes à l'emploi.
 */
package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import kotlinx.coroutines.flow.Flow

@Composable
/**
 * Rôle : Monte l'écran de catalogue des réservants avec son état observable et ses callbacks.
 * Précondition : Les chargeurs, suppressions et callbacks de navigation doivent être fournis par la route.
 * Postcondition : Le catalogue est affiché avec les actions de création, détail, édition et rafraîchissement.
 */
internal fun ReservantsCatalogRoute(
    loadReservants: ReservantsLoader,
    observeReservants: Flow<List<ReservantListItem>>,
    loadDeleteSummary: ReservantDeleteSummaryLoader,
    deleteReservant: ReservantDelete,
    currentUserRole: String?,
    refreshSignal: Int,
    flashMessage: String?,
    onConsumeFlashMessage: () -> Unit,
    onCreateReservant: () -> Unit,
    onOpenReservantDetails: (Int) -> Unit,
    onEditReservant: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ReservantsCatalogViewModel = viewModel(
        factory = reservantsCatalogViewModelFactory(
            loadReservants = loadReservants,
            observeReservants = observeReservants,
            loadDeleteSummary = loadDeleteSummary,
            deleteReservant = deleteReservant,
            currentUserRole = currentUserRole,
        ),
    )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(refreshSignal) {
        if (refreshSignal == 0) {
            return@LaunchedEffect
        }
        viewModel.consumeExternalRefresh(flashMessage)
        onConsumeFlashMessage()
    }

    val actions = remember(viewModel, onCreateReservant, onOpenReservantDetails, onEditReservant) {
        ReservantsCatalogActions(
            onQueryChanged = viewModel::onQueryChanged,
            onTypeSelected = viewModel::onTypeSelected,
            onLinkedEditorOnlyChanged = viewModel::onLinkedEditorOnlyChanged,
            onSortSelected = viewModel::onSortSelected,
            onRefresh = viewModel::refreshReservants,
            onRequestDelete = viewModel::requestDelete,
            onDismissDeleteDialog = viewModel::dismissDeleteDialog,
            onConfirmDelete = viewModel::confirmDelete,
            onDismissInfoMessage = viewModel::dismissInfoMessage,
            onDismissErrorMessage = viewModel::dismissErrorMessage,
            onCreateReservant = onCreateReservant,
            onOpenReservantDetails = onOpenReservantDetails,
            onEditReservant = onEditReservant,
        )
    }

    ReservantsCatalogScreen(
        uiState = uiState.value,
        actions = actions,
        modifier = modifier,
    )
}
