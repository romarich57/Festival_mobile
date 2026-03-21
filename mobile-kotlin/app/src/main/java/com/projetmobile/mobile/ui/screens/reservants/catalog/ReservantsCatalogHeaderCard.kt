package com.projetmobile.mobile.ui.screens.reservants

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.AuthCard
import com.projetmobile.mobile.ui.components.FestivalTextField
import com.projetmobile.mobile.ui.components.PrimaryAuthButton

@Composable
internal fun ReservantsCatalogHeaderCard(
    uiState: ReservantsCatalogUiState,
    actions: ReservantsCatalogActions,
    windowSizeClass: ReservantsWindowSizeClass,
) {
    val stacked = windowSizeClass == ReservantsWindowSizeClass.Compact

    AuthCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reservants-header-card"),
    ) {
        if (stacked) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                CatalogHeaderActions(uiState, actions)
                CatalogFilters(uiState, actions)
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    CatalogHeaderActions(uiState, actions)
                }
                Column(modifier = Modifier.width(320.dp)) {
                    CatalogFilters(uiState, actions)
                }
            }
        }
    }
}

@Composable
private fun CatalogHeaderActions(
    uiState: ReservantsCatalogUiState,
    actions: ReservantsCatalogActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Réservants",
            color = Color(0xFF18233A),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "${uiState.filteredItems.size} affichés sur ${uiState.totalCount}",
            color = Color(0xFF5D6981),
            style = MaterialTheme.typography.bodyLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            if (uiState.canManageReservants) {
                PrimaryAuthButton(
                    text = "Nouveau réservant",
                    modifier = Modifier.height(52.dp),
                    onClick = actions.onCreateReservant,
                )
            }
            OutlinedButton(
                onClick = actions.onRefresh,
                modifier = Modifier.height(52.dp),
            ) {
                Text("Actualiser")
            }
        }
    }
}

@Composable
private fun CatalogFilters(
    uiState: ReservantsCatalogUiState,
    actions: ReservantsCatalogActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        FestivalTextField(
            value = uiState.filters.query,
            onValueChange = actions.onQueryChanged,
            label = "Recherche",
            modifier = Modifier.fillMaxWidth(),
        )
        ReservantsDropdownSelector(
            label = "Type",
            selectedLabel = defaultReservantTypes()
                .firstOrNull { it.value == uiState.filters.selectedType }
                ?.label ?: "Tous",
            options = buildList {
                add("Tous" to null)
                addAll(defaultReservantTypes().map { it.label to it.value })
            },
            onValueSelected = actions.onTypeSelected,
            modifier = Modifier.fillMaxWidth(),
        )
        ReservantsDropdownSelector(
            label = "Tri",
            selectedLabel = uiState.filters.sort.label,
            options = ReservantsSortOption.entries.map { it.label to it },
            onValueSelected = actions.onSortSelected,
            modifier = Modifier.fillMaxWidth(),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = uiState.filters.linkedEditorOnly,
                onClick = { actions.onLinkedEditorOnlyChanged(!uiState.filters.linkedEditorOnly) },
                label = { Text("Avec éditeur lié") },
            )
        }
    }
}
