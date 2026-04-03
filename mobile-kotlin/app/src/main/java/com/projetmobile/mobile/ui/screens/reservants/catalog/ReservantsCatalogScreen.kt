package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthFeedbackBanner
import com.projetmobile.mobile.ui.components.AuthFeedbackTone
import kotlinx.coroutines.delay

@Composable
internal fun ReservantsCatalogScreen(
    uiState: ReservantsCatalogUiState,
    actions: ReservantsCatalogActions,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage == null) {
            return@LaunchedEffect
        }
        delay(4_000)
        actions.onDismissInfoMessage()
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("reservants-catalog-root"),
    ) {
        val windowSizeClass = reservantsWindowSizeClass(maxWidth)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (uiState.errorMessage != null) {
                    item {
                        AuthFeedbackBanner(
                            message = uiState.errorMessage,
                            tone = AuthFeedbackTone.Error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reservants-error-banner"),
                        )
                    }
                }
                if (uiState.infoMessage != null) {
                    item {
                        AuthFeedbackBanner(
                            message = uiState.infoMessage,
                            tone = AuthFeedbackTone.Success,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reservants-info-banner"),
                        )
                    }
                }

                item {
                    ReservantsCatalogHeaderCard(
                        uiState = uiState,
                        actions = actions,
                        windowSizeClass = windowSizeClass,
                    )
                }

                when {
                    uiState.isLoading && uiState.filteredItems.isEmpty() -> item {
                        ReservantsLoadingCard(text = "Chargement des réservants…")
                    }

                    uiState.filteredItems.isEmpty() -> item {
                        ReservantsEmptyCard(
                            title = "Aucun réservant",
                            body = "Aucun résultat ne correspond aux filtres actuels.",
                        )
                    }

                    else -> {
                        items(uiState.filteredItems, key = { it.id }) { reservant ->
                            ReservantCatalogCard(
                                reservant = reservant,
                                canManageReservants = uiState.canManageReservants,
                                canDeleteReservants = uiState.canDeleteReservants,
                                isDeleting = uiState.deletingReservantId == reservant.id,
                                onOpenReservantDetails = actions.onOpenReservantDetails,
                                onEditReservant = actions.onEditReservant,
                                onRequestDelete = actions.onRequestDelete,
                            )
                        }
                    }
                }
            }
        }
    }

    uiState.pendingDeletion?.let { pendingDeletion ->
        ReservantDeleteDialog(
            reservant = pendingDeletion,
            summary = uiState.pendingDeletionSummary,
            isDeleting = uiState.deletingReservantId == pendingDeletion.id,
            onDismiss = actions.onDismissDeleteDialog,
            onConfirm = actions.onConfirmDelete,
        )
    }
}
