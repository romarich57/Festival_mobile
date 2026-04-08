package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.projetmobile.mobile.data.repository.reservation.ReservationRepository
import com.projetmobile.mobile.data.repository.toRepositoryException
import com.projetmobile.mobile.data.repository.zonePlan.ZonePlanRepository
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.addzone.ZoneTarifaireOptionState
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.placement.GameAllocationState
import kotlinx.coroutines.launch
import kotlin.math.ceil

class ZonePlanViewModel(
    internal val zonePlanRepository: ZonePlanRepository,
    internal val reservationRepository: ReservationRepository,
) : ViewModel() {

    var uiState: ZonePlanUiState by mutableStateOf(ZonePlanUiState.Loading)
        internal set

    fun loadContext(reservationId: Int) {
        viewModelScope.launch {
            uiState = ZonePlanUiState.Loading
            try {
                // Fetch reservation details to get the festivalId
                val reservationDetails = reservationRepository.getReservationDetails(reservationId)
                val festivalId = reservationDetails.festivalId
                uiState = fetchState(reservationId, festivalId)
            } catch (throwable: Throwable) {
                uiState = ZonePlanUiState.Error(
                    throwable.toRepositoryException("Impossible de charger le plan de zone.")
                        .localizedMessage
                        ?: "Impossible de charger le plan de zone.",
                )
            }
        }
    }

    fun clearMessage() {
        val current = uiState as? ZonePlanUiState.Success ?: return
        uiState = current.copy(userMessage = null)
    }

    internal suspend fun fetchState(reservationId: Int, festivalId: Int): ZonePlanUiState.Success {
        val context = zonePlanRepository.getZonePlanContext(reservationId, festivalId)
        val reservedZtIds = context.reservedZonesTarifaires.map { it.zoneTarifaireId }.toSet()

        // Build placements list per zone from all_placements + all_game_placements
        val simplePlacementsByZone = context.allPlacements.groupBy { it.zonePlanId }
        val gamePlacementsByZone = context.allGamePlacements.groupBy { it.zonePlanId }

        // Parse zt_available_tables (keys come as strings from JSON)
        val ztAvailable = context.ztAvailableTables.mapKeys { (key, _) -> key.toIntOrNull() ?: 0 }

        val zones = context.zones.map { zp ->
            val simplePlacements = (simplePlacementsByZone[zp.id] ?: emptyList()).map { p ->
                PlacementDisplayItem(
                    id = p.id,
                    reservationId = p.reservationId,
                    reservantName = p.reservantName,
                    gameTitle = null,  // placements simples n'ont pas de jeu
                    nbTables = p.nbTables,
                    tailleTable = p.tailleTable,
                    nbChaises = p.nbChaises,
                    isGamePlacement = false,
                )
            }
            val gamePlacements = (gamePlacementsByZone[zp.id] ?: emptyList()).map { g ->
                PlacementDisplayItem(
                    id = g.allocationId,
                    reservationId = g.reservationId,
                    reservantName = g.reservantName,
                    gameTitle = g.gameTitle,
                    nbTables = ceil(g.nbTablesOccupees * g.nbExemplaires).toInt(),
                    tailleTable = g.tailleTableRequise,
                    nbChaises = g.nbChaises,
                    isGamePlacement = true,
                    allocationId = g.allocationId,
                )
            }

            ZonePlanZoneState(
                id = zp.id,
                name = zp.name,
                zoneTarifaireName = zp.zoneTarifaireName ?: "",
                idZoneTarifaire = zp.idZoneTarifaire,
                totalTables = zp.nbTables,
                allocatedTables = zp.nbTablesAllocated,
                pricePerTable = zp.pricePerTable,
                m2Price = zp.m2Price,
                placements = simplePlacements + gamePlacements,
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
            ztAvailableTables = ztAvailable,
        )
    }

    internal fun sanitizeInt(value: String): String = value.filter { it.isDigit() }

    internal fun sanitizeDecimal(value: String): String =
        value.filter { it.isDigit() || it == '.' || it == ',' }

    companion object {
        internal const val M2_PER_TABLE = 4.5

        fun factory(
            zonePlanRepository: ZonePlanRepository,
            reservationRepository: ReservationRepository,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ZonePlanViewModel(zonePlanRepository, reservationRepository) }
        }
    }
}
