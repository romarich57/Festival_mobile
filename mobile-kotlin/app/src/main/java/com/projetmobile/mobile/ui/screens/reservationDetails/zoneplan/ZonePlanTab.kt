package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.projetmobile.mobile.ui.components.StockSummaryCard
import com.projetmobile.mobile.ui.components.ZonePlanCard
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.addzone.AddZoneFormCard
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.placement.PlacementFormCard

@Composable
fun ZonePlanTab(
    reservationId: Int,
    viewModel: ZonePlanViewModel,
) {
    LaunchedEffect(reservationId) {
        viewModel.loadContext(reservationId)
    }

    when (val state = viewModel.uiState) {
        ZonePlanUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }

        is ZonePlanUiState.Error -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(state.message)
        }

        is ZonePlanUiState.Success -> ZonePlanContent(state, viewModel)
    }
}

@Composable
private fun ZonePlanContent(
    state: ZonePlanUiState.Success,
    viewModel: ZonePlanViewModel,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Title
        item {
            Text(
                text = "Zones de plan",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        // Stock summary
        item {
            StockSummaryCard(state.stock)
        }

        // Success/error message
        if (!state.userMessage.isNullOrBlank()) {
            item {
                Text(
                    text = state.userMessage,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                LaunchedEffect(state.userMessage) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessage()
                }
            }
        }

        // Zones list
        items(state.zones, key = { it.id }) { zone ->
            ZonePlanCard(
                zone = zone,
                currentReservationId = state.reservationId,
                isSaving = state.isSaving,
                onAddPlacement = { viewModel.openPlacementForm(zone.id) },
                onDeletePlacement = { placementId -> viewModel.deleteSimpleAllocation(placementId) },
                onRemoveGame = { allocationId -> viewModel.removeGameFromZone(allocationId) },
                onDeleteZone = { viewModel.deleteZonePlan(zone.id) },
            )
        }

        item {
            Button(
                onClick = { viewModel.openAddZoneForm() },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ajouter une zone de plan")
            }
        }

        if (state.showAddZoneForm) {
            item {
                AddZoneFormCard(
                    form = state.addZoneForm,
                    zonesTarifaires = state.zonesTarifaires,
                    isSaving = state.isSaving,
                    onNameChanged = viewModel::onAddZoneNameChanged,
                    onZoneTarifaireSelected = viewModel::onAddZoneZoneTarifaireSelected,
                    onNbTablesChanged = viewModel::onAddZoneNbTablesChanged,
                    onSave = viewModel::saveAddZone,
                    onCancel = viewModel::closeAddZoneForm,
                )
            }
        }

        // Placement form (if open)
        if (state.showPlacementForm) {
            item {
                PlacementFormCard(
                    form = state.placementForm,
                    unplacedGames = state.games.filter { it.zonePlanId == null },
                    stock = state.stock,
                    isSaving = state.isSaving,
                    onWithGameChanged = viewModel::onWithGameChanged,
                    onGameSelected = viewModel::onGameSelected,
                    onPlacePerCopyChanged = viewModel::onPlacePerCopyChanged,
                    onNbCopiesChanged = viewModel::onNbCopiesChanged,
                    onTableTypeChanged = viewModel::onTableTypeChanged,
                    onChairsChanged = viewModel::onChairsChanged,
                    onNbTablesChanged = viewModel::onNbTablesChanged,
                    onUseM2Changed = viewModel::onUseM2Changed,
                    onM2ValueChanged = viewModel::onM2ValueChanged,
                    onSave = viewModel::savePlacement,
                    onCancel = viewModel::closePlacementForm,
                )
            }
        }
    }
}
