/**
 * Rôle : Structure de données encadrant l'état général du gestionnaire de plan (état isLoading, zones actives).
 *
 * Précondition : Géré et initié par défaut via le ZonePlanViewModel.
 *
 * Postcondition : Reflète l'évolution globale du plan UI.
 */
package com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan

import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.addzone.AddZoneFormState
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.addzone.ZoneTarifaireOptionState
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.placement.GameAllocationState
import com.projetmobile.mobile.ui.screens.reservationDetails.zoneplan.placement.PlacementFormState

/** Represents a single placement displayed in a zone card (from any reservant) */
data class PlacementDisplayItem(
    val id: Int,
    val reservationId: Int,
    val reservantName: String,
    val gameTitle: String? = null,   // null = placement sans jeu
    val nbTables: Int,
    val tailleTable: String = "aucun",
    val nbChaises: Int,
    val isGamePlacement: Boolean = false,  // true = jeux_alloues, false = placement simple
    val allocationId: Int? = null,          // for game placements (jeux_alloues.id)
)

/**
 * Rôle : Décrit l'état immuable du module la zone plan des réservations.
 */
data class ZonePlanZoneState(
    val id: Int,
    val name: String,
    val zoneTarifaireName: String,
    val idZoneTarifaire: Int,
    val totalTables: Int,
    val allocatedTables: Int,
    val pricePerTable: Double,
    val m2Price: Double,
    val placements: List<PlacementDisplayItem> = emptyList(),
    val hasReservationInLinkedZone: Boolean = false,
)

/**
 * Rôle : Décrit l'état immuable du module la zone plan des réservations.
 */
data class StockState(
    val tablesStandard: Int = 0,
    val tablesStandardOccupied: Int = 0,
    val tablesGrande: Int = 0,
    val tablesGrandeOccupied: Int = 0,
    val tablesMairie: Int = 0,
    val tablesMairieOccupied: Int = 0,
    val chaisesTotal: Int = 0,
    val chaisesAllocated: Int = 0,
) {
    /**
     * Rôle : Exécute l'action available for type du module la zone plan des réservations.
     *
     * Précondition : Les données du module doivent être disponibles pour initialiser ou exposer l'état.
     *
     * Postcondition : L'objet retourné décrit un état cohérent et immuable.
     */
    fun availableForType(type: String): Int = when (type) {
        "standard" -> tablesStandard - tablesStandardOccupied
        "grande" -> tablesGrande - tablesGrandeOccupied
        "mairie" -> tablesMairie - tablesMairieOccupied
        else -> Int.MAX_VALUE
    }

    val chaisesAvailable: Int get() = chaisesTotal - chaisesAllocated
}

/**
 * Rôle : Définit le contrat du module la zone plan des réservations.
 */
sealed interface ZonePlanUiState {
    /**
     * Rôle : Expose un singleton de support pour le module la zone plan des réservations.
     */
    data object Loading : ZonePlanUiState

    /**
     * Rôle : Décrit le composant success du module la zone plan des réservations.
     */
    data class Success(
        val reservationId: Int,
        val festivalId: Int,
        val zones: List<ZonePlanZoneState>,
        val games: List<GameAllocationState>,
        val stock: StockState,
        val isSaving: Boolean = false,
        val userMessage: String? = null,
        val showPlacementForm: Boolean = false,
        val placementForm: PlacementFormState = PlacementFormState(),
        val zonesTarifaires: List<ZoneTarifaireOptionState> = emptyList(),
        val showAddZoneForm: Boolean = false,
        val addZoneForm: AddZoneFormState = AddZoneFormState(),
        val ztAvailableTables: Map<Int, Int> = emptyMap(), // zone_tarifaire_id -> tables disponibles
    ) : ZonePlanUiState

    /**
     * Rôle : Décrit le composant erreur du module la zone plan des réservations.
     */
    data class Error(val message: String) : ZonePlanUiState
}
