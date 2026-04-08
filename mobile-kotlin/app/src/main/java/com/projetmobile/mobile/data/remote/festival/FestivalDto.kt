package com.projetmobile.mobile.data.remote.festival

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Rôle : DTO (Data Transfer Object) de réponse spécifique qui enveloppe le [FestivalDto] 
 * renvoyé après une création HTTP victorieuse sur la route `POST /festivals`.
 * 
 * Précondition : Un `POST` a abouti avec un corps JSON contenant `{ "festival": { ... } }`.
 * Postcondition : Extraction possible des informations complètes du nouveau festival.
 */
@Serializable
data class CreateFestivalResponseDto(
    val festival: FestivalDto,
)

/**
 * Rôle : Classe structurante représentant la forme brute d'un Festival échangé avec 
 * le réseau. Gère le mapping dynamique de noms snake_case du serveur vers les 
 * variables camelCase du client Kotlin via l'annotation `@SerialName()`.
 * 
 * Précondition : Donnée extraite du backend au format Json.
 * Postcondition : L'objet pourra être traduit vers le modèle Métier par le Mapper adéquat.
 */
@Serializable
data class FestivalDto(
    val id: Int? = null,
    val name: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    @SerialName("stock_tables_standard") val stockTablesStandard: Int = 0,
    @SerialName("stock_tables_grande") val stockTablesGrande: Int = 0,
    @SerialName("stock_tables_mairie") val stockTablesMairie: Int = 0,
    @SerialName("stock_chaises") val stockChaises: Int = 0,
    @SerialName("prix_prises") val prixPrises: Double = 0.0,
    @SerialName("zones_tarifaires") val zonesTarifaires: List<ZoneTarifaireCreateDto> = emptyList(),
){
    /**
     * Rôle : Propriété calculée dynamique qui fournit le résumé des tables disponibles
     * pour le festival.
     * 
     * Précondition : Champs stock_tables_* chargés.
     * Postcondition : Retourne la somme numérique unifiée des tables.
     */
    val totalTables: Int
        get() = stockTablesStandard + stockTablesGrande + stockTablesMairie

    /**
     * Rôle : DTO imbriqué représentant une tarification ou des emplacements 
     * affectés à un festival spécifique.
     * 
     * Précondition : Associé au paramètre `zones_tarifaires` du payload d'un Festival.
     * Postcondition : Cartographie partielle d'une zone tarifaire reçue en JSON.
     */
    @Serializable
    data class ZoneTarifaireCreateDto(
        val name: String,
        @SerialName("nb_tables") val nbTables: Int,
        @SerialName("price_per_table") val pricePerTable: Double,
    )
}
