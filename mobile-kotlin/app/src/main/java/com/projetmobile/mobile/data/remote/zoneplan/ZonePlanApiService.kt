package com.projetmobile.mobile.data.remote.zoneplan

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Rôle : Déclaration Retrofit des appels réseau orientés "Zone Plan" et gestion spatiale
 * (allocations, placements, création de zones au sein d'un festival, etc.).
 * 
 * Précondition : Utilisation via l'instance Retrofit injectée couplée au Token Manager.
 * Postcondition : Offre la connexion aux endpoints `zone-plan/*` et `jeux_alloues/*`.
 */
interface ZonePlanApiService {

    /**
     * Rôle : Créer une toute nouvelle zone spatiale (`ZonePlan`) rattachée à un festival et 
     * à une tarification (ZoneTarifaire).
     * 
     * Précondition : Payload cohérent avec des IDs de parents qui existent déjà.
     * Postcondition : Zone inscrite en BDD, renvoyée avec son ID assigné.
     */
    @POST("zone-plan")
    suspend fun createZonePlan(
        @Body body: CreateZonePlanDto,
    ): CreateZonePlanResponseDto

    /**
     * Rôle : Obtenir la vision globale de placement pour une Réservation et un Festival :
     * liste des zones, tables libres, jeux non placés, vue complète de tous les placements.
     * 
     * Précondition : Connaître les IDs conjoints de la réservation et du festival.
     * Postcondition : Un Super-DTO centralisant l'état du stock, des listes d'allocations et le plan.
     */
    @GET("zone-plan/reservation/{reservationId}/context/{festivalId}")
    suspend fun getZonePlanContext(
        @Path("reservationId") reservationId: Int,
        @Path("festivalId") festivalId: Int,
    ): ZonePlanContextDto

    /**
     * Rôle : Demande l'allocation "Simple" (sans préciser un jeu, juste des tables/chaises fixes)
     * d'une zone plan pour la réservation spécifiée.
     * 
     * Précondition : Cibler `reservationId` et `zonePlanId`.
     * Postcondition : Insertion de l'allocation ; l'espace global est décrémenté côté backend.
     */
    @POST("zone-plan/reservation/{reservationId}/allocations/{zonePlanId}")
    suspend fun createSimpleAllocation(
        @Path("reservationId") reservationId: Int,
        @Path("zonePlanId") zonePlanId: Int,
        @Body payload: SimpleAllocationPayloadDto,
    ): SimpleAllocationResponseDto

    /**
     * Rôle : Appelle formellement la révocation d'une allocation simple existante.
     * 
     * Précondition : Fournir l'ID exclusif de l'allocation cible.
     * Postcondition : L'espace retourne dans le pool des places disponibles du festival.
     */
    @DELETE("zone-plan/allocations/{allocationId}")
    suspend fun deleteSimpleAllocationById(
        @Path("allocationId") allocationId: Int,
    )

    /**
     * Rôle : Permettre l'affection partielle (PATCH) d'attributs à un jeu alloué, 
     * comme changer sa zone ou le nombre de chaises qu'il requiert.
     * 
     * Précondition : L'allocation cible de jeu doit pré-exister.
     * Postcondition : L'entité cible est ajustée dans la limite de l'inventaire restant.
     */
    @PATCH("jeux_alloues/{allocationId}")
    suspend fun updateGameAllocation(
        @Path("allocationId") allocationId: Int,
        @Body payload: GameAllocationUpdateDto,
    )

    /**
     * Rôle : Force la destruction complète d'une Zone Spatiale (Zone Plan).
     * 
     * Précondition : Ne plus avoir d'allocations obligatoires ou alors la route serveur gère le cascade.
     * Postcondition : Plus aucune donnée liée à cette ID de zone ne perdurera.
     */
    @DELETE("zone-plan/{zonePlanId}")
    suspend fun deleteZonePlan(
        @Path("zonePlanId") zonePlanId: Int,
    )
}
