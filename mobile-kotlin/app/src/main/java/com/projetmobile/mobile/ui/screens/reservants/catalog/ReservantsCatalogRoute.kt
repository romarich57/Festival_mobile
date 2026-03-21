package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun ReservantsCatalogRoute(
    loadReservants: ReservantsLoader,
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
