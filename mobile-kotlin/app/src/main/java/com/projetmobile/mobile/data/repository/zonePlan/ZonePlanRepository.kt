package com.projetmobile.mobile.data.repository.zonePlan

import com.projetmobile.mobile.data.remote.zoneplan.GameAllocationUpdateDto
import com.projetmobile.mobile.data.remote.zoneplan.SimpleAllocationPayloadDto
import com.projetmobile.mobile.data.remote.zoneplan.ZonePlanContextDto

/**
 * Rôle : Abstraction définissant toute l'ingénierie d'allocation physique au sein du festival (Zone Plan).
 * Elle lie un bloc tarifaire, son nombre de tables et la présence/placement des jeux de sociétés.
 * 
 * Précondition : Réservation (ID) du réservant et festival (ID) doivent cohabiter côté serveur.
 * Postcondition : Modèle temps-réel, asynchrone synchrone au réseau vu sa complexité visuelle.
 */
interface ZonePlanRepository {

    /**
     * Rôle : Demander la sculture/création d'un plan d'espace assigné (cluster de tables).
     * 
     * Précondition : Connaissance de la ZoneTarifaire et du quota `nbTables`.
     * Postcondition : Une ZonePlan distante est créée pour le FestivalId en question.
     */
    suspend fun createZonePlan(
        festivalId: Int,
        name: String,
        idZoneTarifaire: Int,
        nbTables: Int,
    )

    /**
     * Rôle : Rapatrier le canevas d'allocation d'une réservation pour comprendre visuellement 
     * ce qui est déjà attribué (tables, m2, listes de jeux).
     * 
     * Précondition : Fiche réservation et fiche festival.
     * Postcondition : Un massif [ZonePlanContextDto] prêt à être désérialisé dans l'UI des plans.
     */
    suspend fun getZonePlanContext(reservationId: Int, festivalId: Int): ZonePlanContextDto

    /**
     * Rôle : Créer un objet d'allocation standard plaçant X tables du Réservant 
     * sur la surface `zonePlanId`.
     * 
     * Précondition : Les quotas initiaux du Plan (nbTables) ne doivent pas être dépassés.
     * Postcondition : Allocation stockée par l'API.
     */
    suspend fun createSimpleAllocation(
        reservationId: Int,
        zonePlanId: Int,
        payload: SimpleAllocationPayloadDto,
    )

    /**
     * Rôle : Libérer une zone allouée en la détruisant côté Backend.
     * 
     * Précondition : L'index exact de l'allocation [allocationId].
     * Postcondition : Les montants de tables sont re-créditées à la zone générale.
     */
    suspend fun deleteSimpleAllocationById(allocationId: Int)

    /**
     * Rôle : Apposer des modifications/quantités sur un emplacement orienté 'Jeux' (GameAllocation).
     * 
     * Précondition : ID GameAllocation [allocationId] et informations mises à jour.
     * Postcondition : Mise à jour en base de données.
     */
    suspend fun updateGameAllocation(allocationId: Int, payload: GameAllocationUpdateDto)

    /**
     * Rôle : Retirer tout un bloc planifié (ZonePlan) : supprime fatalement ses 
     * allocations filles par contrainte SQL/Backend.
     * 
     * Précondition : L'administrateur confirme la destruction de la zone.
     * Postcondition : Espace vaporisé côté API.
     */
    suspend fun deleteZonePlan(zonePlanId: Int)
}