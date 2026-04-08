package com.projetmobile.mobile.data.remote.zoneplan

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rôle : Collection des Data Transfer Objects dédiés au nom de l'affectation de l'espace (Plans de Zones, Placements et Allocations).
 * 
 * Précondition : Importation par Kotlinx Serialization.
 * Postcondition : Garantit la conversion entre le Json brut du backend et le Kotlin.
 */

/** Rôle : Structurer une requête de nouvelle zone de plan. */
@Serializable
data class CreateZonePlanDto(
    val name: String,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("id_zone_tarifaire") val idZoneTarifaire: Int,
    @SerialName("nb_tables") val nbTables: Int,
)

/** Rôle : Envelopper le message de réussite lors de l'ajout d'une zone. */
@Serializable
data class CreateZonePlanResponseDto(
    val message: String,
    @SerialName("zone_plan") val zonePlan: ZonePlanDto,
)

/** Rôle : Représenter une entité ZonePlan de la BDD. */
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

/** Rôle : Définir le placement d'un jeu précis (combien de tables lui sont dédiées, chaises...). */
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

/** Rôle : Affectation libre (hors jeux) à un emplacement pour un exposant. */
@Serializable
data class SimpleAllocationDto(
    val id: Int = 0,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("zone_plan_id") val zonePlanId: Int,
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("nb_chaises") val nbChaises: Int = 0,
    @SerialName("taille_table") val tailleTable: String = "aucun",
)

/** Rôle : Bilan récapitulatif pour repérer le solde de réservation contracté sur une zone tarifaire. */
@Serializable
data class ReservedZoneTarifaireDto(
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("nb_tables_reservees") val nbTablesReservees: Int,
    @SerialName("zone_name") val zoneName: String = "",
)

/** Rôle : Structure imbriqué pour calculer l'état (libre/occupé/loué) d'un mobilier. */
@Serializable
data class StockEntryDto(
    val total: Int = 0,
    val occupied: Int = 0,
    val allocated: Int = 0,
)

/** Rôle : Synthétise l'inventaire physique restants des mobiliers d'un festival. */
@Serializable
data class StockDto(
    @SerialName("tables_standard") val tablesStandard: StockEntryDto = StockEntryDto(),
    @SerialName("tables_grande") val tablesGrande: StockEntryDto = StockEntryDto(),
    @SerialName("tables_mairie") val tablesMairie: StockEntryDto = StockEntryDto(),
    val chaises: StockEntryDto = StockEntryDto(),
)

/** 
 * Rôle : Représente un placement simple visible dans une zone (tous réservants confondus).
 * Permet au tableau de bord géant de connaître qui est assis où.
 */
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

/** 
 * Rôle : Représente un jeu placé dans une zone (tous réservants confondus). 
 * Contrairement au SimplePlacement, expose le nom du jeu.
 */
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

/**
 * Rôle : Objet "Monstrueux" consolidant toute l'information spatiale et logistique 
 * en une seule route API majeure pour gagner en rapidité coté client.
 */
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

/** Rôle : Payload émis pour créer une allocation "simple" (non ludique). */
@Serializable
data class SimpleAllocationPayloadDto(
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("nb_chaises") val nbChaises: Int = 0,
    @SerialName("taille_table") val tailleTable: String = "aucun",
)

/** Rôle : Réponse de succès lors de la création d'allocation simple. */
@Serializable
data class SimpleAllocationResponseDto(
    val id: Int,
    @SerialName("reservation_id") val reservationId: Int,
    @SerialName("zone_plan_id") val zonePlanId: Int,
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("nb_chaises") val nbChaises: Int = 0,
    @SerialName("taille_table") val tailleTable: String = "aucun",
)

/** Rôle : Payload `PATCH` pour changer certaines caractéristiques d'une GameAllocation seulement. */
@Serializable
data class GameAllocationUpdateDto(
    @SerialName("zone_plan_id") val zonePlanId: Int? = null,
    @SerialName("nb_tables_occupees") val nbTablesOccupees: Double? = null,
    @SerialName("nb_exemplaires") val nbExemplaires: Double? = null,
    @SerialName("nb_chaises") val nbChaises: Int? = null,
    @SerialName("taille_table_requise") val tailleTableRequise: String? = null,
)
