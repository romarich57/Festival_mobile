package com.projetmobile.mobile.ui.screens.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantListItem

internal data class ReservantsCatalogActions(
    val onQueryChanged: (String) -> Unit,
    val onTypeSelected: (String?) -> Unit,
    val onLinkedEditorOnlyChanged: (Boolean) -> Unit,
    val onSortSelected: (ReservantsSortOption) -> Unit,
    val onRefresh: () -> Unit,
    val onDismissDeleteDialog: () -> Unit,
    val onConfirmDelete: () -> Unit,
    val onDismissInfoMessage: () -> Unit,
    val onDismissErrorMessage: () -> Unit,
    val onRequestDelete: (ReservantListItem) -> Unit,
    val onCreateReservant: () -> Unit,
    val onOpenReservantDetails: (Int) -> Unit,
    val onEditReservant: (Int) -> Unit,
)
