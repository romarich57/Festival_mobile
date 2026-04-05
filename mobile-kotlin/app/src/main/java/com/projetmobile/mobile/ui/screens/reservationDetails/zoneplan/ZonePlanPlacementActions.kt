package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.remote.zoneplan.GameAllocationUpdateDto
import com.projetmobile.mobile.data.remote.zoneplan.SimpleAllocationPayloadDto
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.placement.PlacementFormState
import kotlinx.coroutines.launch
import kotlin.math.ceil

fun ZonePlanViewModel.openPlacementForm(zonePlanId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        showPlacementForm = true,
        placementForm = PlacementFormState(zonePlanId = zonePlanId),
    )
}

fun ZonePlanViewModel.closePlacementForm() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(showPlacementForm = false)
}

fun ZonePlanViewModel.onWithGameChanged(value: Boolean) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(
            withGame = value,
            selectedGameAllocationId = null,
        ),
    )
}

fun ZonePlanViewModel.onGameSelected(allocationId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(selectedGameAllocationId = allocationId),
    )
}

fun ZonePlanViewModel.onPlacePerCopyChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(placePerCopy = sanitizeDecimal(value)),
    )
}

fun ZonePlanViewModel.onNbCopiesChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(nbCopies = sanitizeDecimal(value)),
    )
}

fun ZonePlanViewModel.onTableTypeChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(tableType = value),
    )
}

fun ZonePlanViewModel.onChairsChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(nbChaises = sanitizeInt(value)),
    )
}

fun ZonePlanViewModel.onNbTablesChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(nbTables = sanitizeInt(value)),
    )
}

fun ZonePlanViewModel.onUseM2Changed(value: Boolean) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(
        placementForm = current.placementForm.copy(useM2 = value, m2Value = "", nbTables = "1"),
    )
}

fun ZonePlanViewModel.onM2ValueChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val sanitized = sanitizeDecimal(value)
    val m2 = sanitized.replace(',', '.').toDoubleOrNull() ?: 0.0
    val tables = if (m2 > 0) ceil(m2 / ZonePlanViewModel.M2_PER_TABLE).toInt() else 0
    uiState = current.copy(
        placementForm = current.placementForm.copy(m2Value = sanitized, nbTables = tables.toString()),
    )
}

fun ZonePlanViewModel.savePlacement() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val form = current.placementForm
    val zone = current.zones.find { it.id == form.zonePlanId } ?: return

    // Validate: reservant must have tables in linked zone tarifaire
    if (!zone.hasReservationInLinkedZone) {
        uiState = current.copy(userMessage = "Pas de tables réservées dans la zone tarifaire liée")
        return
    }

    viewModelScope.launch {
        uiState = current.copy(isSaving = true, userMessage = null)
        try {
            if (form.withGame) {
                saveGamePlacement(current)
            } else {
                saveSimplePlacement(current)
            }
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(
                showPlacementForm = false,
                userMessage = "Placement enregistré",
            )
        } catch (e: Exception) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(isSaving = false, userMessage = "Erreur: ${e.message}")
            }
        }
    }
}

/** Supprimer un placement simple par son ID unique */
fun ZonePlanViewModel.deleteSimpleAllocation(allocationId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    viewModelScope.launch {
        uiState = current.copy(isSaving = true)
        try {
            zonePlanRepository.deleteSimpleAllocationById(allocationId)
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(userMessage = "Placement supprimé")
        } catch (e: Exception) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(isSaving = false, userMessage = "Erreur: ${e.message}")
            }
        }
    }
}

fun ZonePlanViewModel.removeGameFromZone(allocationId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    viewModelScope.launch {
        uiState = current.copy(isSaving = true)
        try {
            zonePlanRepository.updateGameAllocation(
                allocationId,
                GameAllocationUpdateDto(zonePlanId = null),
            )
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(userMessage = "Jeu retiré de la zone")
        } catch (e: Exception) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(isSaving = false, userMessage = "Erreur: ${e.message}")
            }
        }
    }
}

internal suspend fun ZonePlanViewModel.saveSimplePlacement(state: ZonePlanUiState.Success) {
    val form = state.placementForm
    val nbTables = form.nbTables.toIntOrNull() ?: 0
    val nbChaises = form.nbChaises.toIntOrNull() ?: 0
    if (nbTables <= 0 && nbChaises <= 0) throw IllegalArgumentException("Tables ou chaises requis")

    zonePlanRepository.createSimpleAllocation(
        reservationId = state.reservationId,
        zonePlanId = form.zonePlanId,
        payload = SimpleAllocationPayloadDto(
            nbTables = nbTables,
            nbChaises = nbChaises,
            tailleTable = form.tableType,
        ),
    )
}

internal suspend fun ZonePlanViewModel.saveGamePlacement(state: ZonePlanUiState.Success) {
    val form = state.placementForm
    val allocationId = form.selectedGameAllocationId
        ?: throw IllegalArgumentException("Sélectionnez un jeu")

    val placePerCopy = form.placePerCopy.replace(',', '.').toDoubleOrNull() ?: 1.0
    val nbCopies = form.nbCopies.replace(',', '.').toDoubleOrNull() ?: 1.0
    val nbChaises = form.nbChaises.toIntOrNull() ?: 0

    zonePlanRepository.updateGameAllocation(
        allocationId = allocationId,
        payload = GameAllocationUpdateDto(
            zonePlanId = form.zonePlanId,
            nbTablesOccupees = placePerCopy,
            nbExemplaires = nbCopies,
            nbChaises = nbChaises,
            tailleTableRequise = form.tableType,
        ),
    )
}
