package com.projetmobile.mobile.data.remote.reservation

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rôle : Renseigne virtuellement dans la base distante un secteur (zone tarifaire).
 * 
 * Précondition : Reçue via la réponse JSON GET zones-tarifaires.
 * Postcondition : Contient tous les montants (à la table, au m²) ainsi que l'usure de l'espace.
 */
@Serializable
data class ZoneTarifaireDto(
    val id: Int,
    val name: String,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("nb_tables") val nbTables: Int,
    @SerialName("price_per_table") val pricePerTable: Double,
    @SerialName("nb_tables_available") val nbTablesAvailable: Int,
    @SerialName("m2_price") val m2Price: Double,
)

/**
 * Rôle : Décrit l'association locale et pécunière d'une réservation à une de ces zones tarifaires.
 * 
 * Précondition : C'est une sous-partie imbriquée dans le JSON Réservation détails.
 * Postcondition : Indique où se situera l'interlocuteur.
 */
@Serializable
data class ReservationZoneTarifaireDto(
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("nb_tables_reservees") val nbTablesReservees: Int,
    @SerialName("nb_chaises_reservees") val nbChaisesReservees: Int = 0,
    @SerialName("zone_name") val zoneName: String = "",
    @SerialName("price_per_table") val pricePerTable: Double = 0.0,
    @SerialName("nb_tables_available") val nbTablesAvailable: Int = 0,
)

/**
 * Rôle : Le Master-DTO affichant tout ce qui englobe financièrement et 
 * formellement une réservation. Unifie prix, rabais, notes et listes des emplacements.
 * 
 * Précondition : Appel de requète vers un détail unitaire de réservation.
 * Postcondition : Sert à peupler tout le document de caisse côté client.
 */
@Serializable
data class ReservationDetailsDto(
    val id: Int,
    @SerialName("festival_id") val festivalId: Int,
    @SerialName("start_price") val startPrice: Double = 0.0,
    @SerialName("table_discount_offered") val tableDiscountOffered: Double = 0.0,
    @SerialName("direct_discount") val directDiscount: Double = 0.0,
    @SerialName("nb_prises") val nbPrises: Int = 0,
    @SerialName("final_price") val finalPrice: Double = 0.0,
    val note: String? = null,
    @SerialName("zones_tarifaires") val zonesTarifaires: List<ReservationZoneTarifaireDto> = emptyList(),
)

/**
 * Rôle : Segment envoyé de modification pour ajuster le quota accordé à un exposant dans une zone.
 * 
 * Précondition : Fait partie de l'objet globale ReservationUpdatePayload.
 * Postcondition : Surcharge en Base de Données le volume mobilisé par cet usager.
 */
@Serializable
data class ReservationZoneUpdateDto(
    @SerialName("zone_tarifaire_id") val zoneTarifaireId: Int,
    @SerialName("nb_tables_reservees") val nbTablesReservees: Int,
    @SerialName("nb_chaises_reservees") val nbChaisesReservees: Int = 0,
)

/**
 * Rôle : Paquet récapitulant les modulations d'une note de frais/espace à surécrire (Update) via PUT.
 * 
 * Précondition : Toutes les valeurs (mème les non-modifiées) doivent figurer.
 * Postcondition : Sauvegarde centralisée et validation coté serveur de la facture en écrasement.
 */
@Serializable
data class ReservationUpdatePayloadDto(
    @SerialName("start_price") val startPrice: Double,
    @SerialName("nb_prises") val nbPrises: Int,
    @SerialName("final_price") val finalPrice: Double,
    @SerialName("table_discount_offered") val tableDiscountOffered: Double,
    @SerialName("direct_discount") val directDiscount: Double,
    val note: String? = null,
    @SerialName("zones_tarifaires") val zonesTarifaires: List<ReservationZoneUpdateDto> = emptyList(),
)
