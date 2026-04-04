package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.remote.zoneplan.GameAllocationUpdateDto
import com.projetmobile.mobile.data.remote.zoneplan.SimpleAllocationPayloadDto
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import com.projetmobile.mobile.data.repository.zonePlan.ZonePlanRepository
import kotlinx.coroutines.launch
import kotlin.math.ceil

class ZonePlanViewModel(
    private val zonePlanRepository: ZonePlanRepository,
    private val reservationRepository: ReservationRepository,
) : ViewModel() {

    var uiState: ZonePlanUiState by mutableStateOf(ZonePlanUiState.Loading)
        private set

    fun loadContext(reservationId: Int) {
        viewModelScope.launch {
            uiState = ZonePlanUiState.Loading
            try {
                // Fetch reservation details to get the festivalId
                val reservationDetails = reservationRepository.getReservationDetails(reservationId)
                val festivalId = reservationDetails.festivalId
                uiState = fetchState(reservationId, festivalId)
            } catch (e: Exception) {
                uiState = ZonePlanUiState.Error("Erreur réseau : ${e.message}")
            }
        }
    }

    // ── Form actions ─────────────────────────────────────────────────────────

    fun openPlacementForm(zonePlanId: Int) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(
            showPlacementForm = true,
            placementForm = PlacementFormState(zonePlanId = zonePlanId),
        )
    }

    fun closePlacementForm() {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(showPlacementForm = false)
    }

    fun onWithGameChanged(value: Boolean) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(
            placementForm = current.placementForm.copy(
                withGame = value,
                selectedGameAllocationId = null,
            ),
        )
    }

    fun onGameSelected(allocationId: Int) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(
            placementForm = current.placementForm.copy(selectedGameAllocationId = allocationId),
        )
    }

    fun onPlacePerCopyChanged(value: String) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(
            placementForm = current.placementForm.copy(placePerCopy = sanitizeDecimal(value)),
        )
    }

    fun onNbCopiesChanged(value: String) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(
            placementForm = current.placementForm.copy(nbCopies = sanitizeDecimal(value)),
        )
    }

    fun onTableTypeChanged(value: String) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(
            placementForm = current.placementForm.copy(tableType = value),
        )
    }

    fun onChairsChanged(value: String) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(
            placementForm = current.placementForm.copy(nbChaises = sanitizeInt(value)),
        )
    }

    fun onNbTablesChanged(value: String) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(
            placementForm = current.placementForm.copy(nbTables = sanitizeInt(value)),
        )
    }

    fun onUseM2Changed(value: Boolean) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(
            placementForm = current.placementForm.copy(useM2 = value, m2Value = "", nbTables = "1"),
        )
    }

    fun onM2ValueChanged(value: String) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        val sanitized = sanitizeDecimal(value)
        val m2 = sanitized.replace(',', '.').toDoubleOrNull() ?: 0.0
        val tables = if (m2 > 0) ceil(m2 / M2_PER_TABLE).toInt() else 0
        uiState = current.copy(
            placementForm = current.placementForm.copy(m2Value = sanitized, nbTables = tables.toString()),
        )
    }

    fun clearMessage() {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(userMessage = null)
    }

    // ── Save ─────────────────────────────────────────────────────────────────

    fun savePlacement() {
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

    fun deleteSimpleAllocation(zonePlanId: Int) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        viewModelScope.launch {
            uiState = current.copy(isSaving = true)
            try {
                zonePlanRepository.deleteSimpleAllocation(current.reservationId, zonePlanId)
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

    fun removeGameFromZone(allocationId: Int) {
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

    // ── Private helpers ──────────────────────────────────────────────────────

    private suspend fun saveSimplePlacement(state: ZonePlanUiState.Success) {
        val form = state.placementForm
        val nbTables = form.nbTables.toIntOrNull() ?: 0
        val nbChaises = form.nbChaises.toIntOrNull() ?: 0
        if (nbTables <= 0 && nbChaises <= 0) throw IllegalArgumentException("Tables ou chaises requis")

        zonePlanRepository.upsertSimpleAllocation(
            reservationId = state.reservationId,
            zonePlanId = form.zonePlanId,
            payload = SimpleAllocationPayloadDto(
                nbTables = nbTables,
                nbChaises = nbChaises,
                tailleTable = form.tableType,
            ),
        )
    }

    private suspend fun saveGamePlacement(state: ZonePlanUiState.Success) {
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

    private suspend fun fetchState(reservationId: Int, festivalId: Int): ZonePlanUiState.Success {
        val context = zonePlanRepository.getZonePlanContext(reservationId, festivalId)
        val reservedZtIds = context.reservedZonesTarifaires.map { it.zoneTarifaireId }.toSet()
        val simpleByZone = context.simpleAllocations.associateBy { it.zonePlanId }

        val zones = context.zones.map { zp ->
            val simple = simpleByZone[zp.id]
            ZonePlanZoneState(
                id = zp.id,
                name = zp.name,
                zoneTarifaireName = zp.zoneTarifaireName ?: "",
                idZoneTarifaire = zp.idZoneTarifaire,
                totalTables = zp.nbTables,
                allocatedTables = zp.nbTablesAllocated,
                pricePerTable = zp.pricePerTable,
                m2Price = zp.m2Price,
                mySimpleAllocationTables = simple?.nbTables ?: 0,
                mySimpleAllocationChaises = simple?.nbChaises ?: 0,
                hasReservationInLinkedZone = zp.idZoneTarifaire in reservedZtIds,
            )
        }

        // All games for the reservant (placed and unplaced)
        val games = context.unplacedGames.map { g ->
            GameAllocationState(
                allocationId = g.allocationId,
                gameId = g.gameId,
                gameTitle = g.gameTitle,
                gameType = g.gameType,
                nbTablesOccupees = g.nbTablesOccupees,
                nbExemplaires = g.nbExemplaires,
                nbChaises = g.nbChaises,
                tailleTableRequise = g.tailleTableRequise,
                zonePlanId = g.zonePlanId,
            )
        }

        val stock = StockState(
            tablesStandard = context.stock.tablesStandard.total,
            tablesStandardOccupied = context.stock.tablesStandard.occupied,
            tablesGrande = context.stock.tablesGrande.total,
            tablesGrandeOccupied = context.stock.tablesGrande.occupied,
            tablesMairie = context.stock.tablesMairie.total,
            tablesMairieOccupied = context.stock.tablesMairie.occupied,
            chaisesTotal = context.stock.chaises.total,
            chaisesAllocated = context.stock.chaises.allocated,
        )

        val zonesTarifaires = reservationRepository.getZonesTarifaires(festivalId).map {
            ZoneTarifaireOptionState(id = it.id, name = it.name, nbTables = it.nbTables)
        }

        return ZonePlanUiState.Success(
            reservationId = reservationId,
            festivalId = festivalId,
            zones = zones,
            games = games,
            stock = stock,
            zonesTarifaires = zonesTarifaires,
        )
    }

    private fun sanitizeInt(value: String): String = value.filter { it.isDigit() }

    private fun sanitizeDecimal(value: String): String = value.filter { it.isDigit() || it == '.' || it == ',' }

    companion object {
        private const val M2_PER_TABLE = 4.5

        fun factory(
            zonePlanRepository: ZonePlanRepository,
            reservationRepository: ReservationRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ZonePlanViewModel(zonePlanRepository, reservationRepository) }
        }
    }


    fun openAddZoneForm() {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(showAddZoneForm = true, addZoneForm = AddZoneFormState())
    }

    fun closeAddZoneForm() {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(showAddZoneForm = false)
    }

    fun onAddZoneNameChanged(value: String) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(addZoneForm = current.addZoneForm.copy(name = value))
    }

    fun onAddZoneZoneTarifaireSelected(id: Int) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        val zt = current.zonesTarifaires.find { it.id == id } ?: return
        uiState = current.copy(
            addZoneForm = current.addZoneForm.copy(
                selectedZoneTarifaireId = id,
                maxTables = zt.nbTables,
                // Reset nb tables si dépasse le nouveau max
                nbTables = current.addZoneForm.nbTables.toIntOrNull()
                    ?.coerceAtMost(zt.nbTables)?.toString() ?: "",
            )
        )
    }

    fun onAddZoneNbTablesChanged(value: String) {
        val current = uiState as? ZonePlanUiState.Success ?: return
        val sanitized = sanitizeInt(value)
        uiState = current.copy(addZoneForm = current.addZoneForm.copy(nbTables = sanitized))
    }

    fun saveAddZone() {
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
            uiState = current.copy(userMessage = "Maximum ${form.maxTables} tables pour cette zone tarifaire")
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
            } catch (e: Exception) {
                val latest = uiState as? ZonePlanUiState.Success
                if (latest != null) {
                    uiState = latest.copy(isSaving = false, userMessage = "Erreur: ${e.message}")
                }
            }
        }
    }



}
