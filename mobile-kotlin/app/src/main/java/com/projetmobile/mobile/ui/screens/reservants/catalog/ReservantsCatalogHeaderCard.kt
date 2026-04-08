/**
 * Rôle : Construit l'en-tête du catalogue des réservants avec filtres, recherche et actions principales.
 * Ce fichier regroupe les sous-sections qui pilotent l'expérience de filtrage du catalogue.
 * Précondition : L'état du catalogue et les callbacks de navigation doivent être connus.
 * Postcondition : L'utilisateur peut rechercher, filtrer, trier et déclencher une création ou un rafraîchissement.
 */
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
/**
 * Rôle : Affiche l'en-tête du catalogue des réservants en version compacte ou étendue selon la largeur disponible.
 * Précondition : `uiState`, `actions` et `windowSizeClass` doivent être cohérents avec l'écran courant.
 * Postcondition : Le catalogue reçoit un header adaptable et prêt à piloter les filtres.
 */
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
/**
 * Rôle : Affiche le titre du catalogue, les compteurs et les actions principales de l'en-tête.
 * Précondition : `uiState` doit contenir les données agrégées du catalogue.
 * Postcondition : L'utilisateur voit le contexte global du catalogue et les actions de premier niveau.
 */
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
/**
 * Rôle : Affiche les filtres de recherche, de type, de tri et de liaison éditeur du catalogue.
 * Précondition : `uiState` doit exposer les filtres courants et `actions` leurs callbacks de mise à jour.
 * Postcondition : Les filtres visibles reflètent l'état courant et renvoient les changements à la couche logique.
 */
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
