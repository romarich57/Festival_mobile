package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

import androidx.lifecycle.viewModelScope
import com.projetmobile.mobile.data.repository.toRepositoryException
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.addzone.AddZoneFormState
import kotlinx.coroutines.launch

fun ZonePlanViewModel.openAddZoneForm() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(showAddZoneForm = true, addZoneForm = AddZoneFormState())
}

fun ZonePlanViewModel.closeAddZoneForm() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(showAddZoneForm = false)
}

fun ZonePlanViewModel.onAddZoneNameChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    uiState = current.copy(addZoneForm = current.addZoneForm.copy(name = value))
}

fun ZonePlanViewModel.onAddZoneZoneTarifaireSelected(id: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val zt = current.zonesTarifaires.find { it.id == id } ?: return
    // Calculer les tables disponibles : total ZT - somme des zones plan existantes sur cette ZT
    val available = current.ztAvailableTables[id] ?: zt.nbTables
    uiState = current.copy(
        addZoneForm = current.addZoneForm.copy(
            selectedZoneTarifaireId = id,
            maxTables = available,
            // Reset nb tables si dépasse le nouveau max
            nbTables = current.addZoneForm.nbTables.toIntOrNull()
                ?.coerceAtMost(available)?.toString() ?: "",
        ),
    )
}

fun ZonePlanViewModel.onAddZoneNbTablesChanged(value: String) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val sanitized = sanitizeInt(value)
    uiState = current.copy(addZoneForm = current.addZoneForm.copy(nbTables = sanitized))
}

fun ZonePlanViewModel.saveAddZone() {
    val current = uiState as? ZonePlanUiState.Success ?: return
    val form = current.addZoneForm

    val name = form.name.trim()
    val ztId = form.selectedZoneTarifaireId
    val nbTables = form.nbTables.toIntOrNull() ?: 0

    if (name.isBlank()) {
        uiState = current.copy(userMessage = "Le nom est requis")
        return
    }
    if (ztId == null) {
        uiState = current.copy(userMessage = "Sélectionnez une zone tarifaire")
        return
    }
    if (nbTables <= 0) {
        uiState = current.copy(userMessage = "Nombre de tables invalide")
        return
    }
    if (nbTables > form.maxTables) {
        uiState = current.copy(userMessage = "Maximum ${form.maxTables} tables disponibles pour cette zone tarifaire")
        return
    }

    viewModelScope.launch {
        uiState = current.copy(isSaving = true, userMessage = null)
        try {
            zonePlanRepository.createZonePlan(
                festivalId = current.festivalId,
                name = name,
                idZoneTarifaire = ztId,
                nbTables = nbTables,
            )
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(showAddZoneForm = false, userMessage = "Zone créée")
        } catch (throwable: Throwable) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(
                    isSaving = false,
                    userMessage = throwable.zonePlanAddZoneErrorMessage("Impossible de créer la zone."),
                )
            }
        }
    }
}

fun ZonePlanViewModel.deleteZonePlan(zonePlanId: Int) {
    val current = uiState as? ZonePlanUiState.Success ?: return
    viewModelScope.launch {
        uiState = current.copy(isSaving = true)
        try {
            zonePlanRepository.deleteZonePlan(zonePlanId)
            val refreshed = fetchState(current.reservationId, current.festivalId)
            uiState = refreshed.copy(userMessage = "Zone de plan supprimée")
        } catch (throwable: Throwable) {
            val latest = uiState as? ZonePlanUiState.Success
            if (latest != null) {
                uiState = latest.copy(
                    isSaving = false,
                    userMessage = throwable.zonePlanAddZoneErrorMessage("Impossible de supprimer la zone."),
                )
            }
        }
    }
}

private fun Throwable.zonePlanAddZoneErrorMessage(defaultMessage: String): String {
    return toRepositoryException(defaultMessage).localizedMessage ?: defaultMessage
}
