package com.projetmobile.mobile.data.remote.zoneplan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class CreateZonePlanDto(
    val name: String,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("id_zone_tarifaire") val idZoneTarifaire: Int,
    @SerialName("nb_tables") val nbTables: Int,
)

@Serializable
data class CreateZonePlanResponseDto(
    val message: String,
    @SerialName("zone_plan") val zonePlan: ZonePlanDto,
)
@Serializable
data class ZonePlanDto(
    val id: Int,
    val name: String,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("id_zone_tarifaire") val idZoneTarifaire: Int,
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("zone_tarifaire_name") val zoneTarifaireName: String? = null,
    @SerialName("price_per_table") val pricePerTable: Double = 0.0,
    @SerialName("m2_price") val m2Price: Double = 0.0,
    @SerialName("nb_tables_allocated") val nbTablesAllocated: Int = 0,
)

@Serializable
data class GameAllocationDto(
    @SerialName("allocation_id") val allocationId: Int,
    @SerialName("game_id") val gameId: Int,
    @SerialName("nb_tables_occupees") val nbTablesOccupees: Double,
    @SerialName("nb_exemplaires") val nbExemplaires: Double,
    @SerialName("nb_chaises") val nbChaises: Int = 0,
    @SerialName("taille_table_requise") val tailleTableRequise: String = "standard",
    @SerialName("zone_plan_id") val zonePlanId: Int? = null,
    @SerialName("game_title") val gameTitle: String = "",
    @SerialName("game_type") val gameType: String = "",
)

@Serializable
data class SimpleAllocationDto(
    val id: Int = 0,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("zone_plan_id") val zonePlanId: Int,
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("nb_chaises") val nbChaises: Int = 0,
    @SerialName("taille_table") val tailleTable: String = "aucun",
)

@Serializable
data class ReservedZoneTarifaireDto(
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("nb_tables_reservees") val nbTablesReservees: Int,
    @SerialName("zone_name") val zoneName: String = "",
)

@Serializable
data class StockEntryDto(
    val total: Int = 0,
    val occupied: Int = 0,
    val allocated: Int = 0,
)

@Serializable
data class StockDto(
    @SerialName("tables_standard") val tablesStandard: StockEntryDto = StockEntryDto(),
    @SerialName("tables_grande") val tablesGrande: StockEntryDto = StockEntryDto(),
    @SerialName("tables_mairie") val tablesMairie: StockEntryDto = StockEntryDto(),
    val chaises: StockEntryDto = StockEntryDto(),
)

/** Représente un placement simple visible dans une zone (tous réservants confondus) */
@Serializable
data class ZonePlacementDto(
    val id: Int,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("zone_plan_id") val zonePlanId: Int,
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("nb_chaises") val nbChaises: Int = 0,
    @SerialName("taille_table") val tailleTable: String = "aucun",
    @SerialName("reservant_name") val reservantName: String = "",
)

/** Représente un jeu placé dans une zone (tous réservants confondus) */
@Serializable
data class GamePlacementDto(
    @SerialName("allocation_id") val allocationId: Int,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("zone_plan_id") val zonePlanId: Int,
    @SerialName("nb_tables_occupees") val nbTablesOccupees: Double,
    @SerialName("nb_exemplaires") val nbExemplaires: Double,
    @SerialName("nb_chaises") val nbChaises: Int = 0,
    @SerialName("taille_table_requise") val tailleTableRequise: String = "aucun",
    @SerialName("game_title") val gameTitle: String = "",
    @SerialName("reservant_name") val reservantName: String = "",
)

@Serializable
data class ZonePlanContextDto(
    val zones: List<ZonePlanDto> = emptyList(),
    @SerialName("unplaced_games") val unplacedGames: List<GameAllocationDto> = emptyList(),
    @SerialName("simple_allocations") val simpleAllocations: List<SimpleAllocationDto> = emptyList(),
    @SerialName("reserved_zones_tarifaires") val reservedZonesTarifaires: List<ReservedZoneTarifaireDto> = emptyList(),
    val stock: StockDto = StockDto(),
    @SerialName("all_placements") val allPlacements: List<ZonePlacementDto> = emptyList(),
    @SerialName("all_game_placements") val allGamePlacements: List<GamePlacementDto> = emptyList(),
    @SerialName("zt_available_tables") val ztAvailableTables: Map<String, Int> = emptyMap(),
)

@Serializable
data class SimpleAllocationPayloadDto(
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("nb_chaises") val nbChaises: Int = 0,
    @SerialName("taille_table") val tailleTable: String = "aucun",
)

@Serializable
data class SimpleAllocationResponseDto(
    val id: Int,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("zone_plan_id") val zonePlanId: Int,
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("nb_chaises") val nbChaises: Int = 0,
    @SerialName("taille_table") val tailleTable: String = "aucun",
)

@Serializable
data class GameAllocationUpdateDto(
    @SerialName("zone_plan_id") val zonePlanId: Int? = null,
    @SerialName("nb_tables_occupees") val nbTablesOccupees: Double? = null,
    @SerialName("nb_exemplaires") val nbExemplaires: Double? = null,
    @SerialName("nb_chaises") val nbChaises: Int? = null,
    @SerialName("taille_table_requise") val tailleTableRequise: String? = null,
)
