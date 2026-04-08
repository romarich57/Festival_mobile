package com.projetmobile.mobile.data.remote.reservants

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Rôle : Service API Retrofit assurant les opérations réseau sur les entités "Reservant"
 * (éditeurs, créateurs de jeux...). Intègre la lecture, modification, association
 * de contacts et l'évaluation prédictive des dépendances en cas de suppression.
 * 
 * Précondition : Le client HTTP (OkHttp) doit fournir le jeton d'authentification adéquat.
 * Postcondition : Offre les suspend functions appelées par les Repositories.
 */
interface ReservantsApiService {

    /**
     * Rôle : Récupère la liste de tous les réservants enregistrés.
     * 
     * Précondition : Connexion au serveur active.
     * Postcondition : Renvoie un tableau des DTO de réservants de la base de données distante.
     */
    @GET("reservant")
    suspend fun getReservants(): List<ReservantDto>

    /**
     * Rôle : Interroge les détails formels d'un unique réservant.
     * 
     * Précondition : ID distant du réservant ciblé.
     * Postcondition : Obtention des datas de type [ReservantDto].
     */
    @GET("reservant/{id}")
    suspend fun getReservant(@Path("id") reservantId: Int): ReservantDto

    /**
     * Rôle : Crée (`POST`) un tout nouveau réservant côté backend.
     * 
     * Précondition : Le corps contient les attributs attendus ([ReservantUpsertRequestDto]).
     * Postcondition : Validation, affectation d'ID serveur, et retour en format réponse.
     */
    @POST("reservant")
    suspend fun createReservant(@Body request: ReservantUpsertRequestDto): ReservantDto

    /**
     * Rôle : Écrase les propriétés (`PUT`) d'un réservant déjà existant par son ID.
     * 
     * Précondition : ID spécifique et objet [ReservantUpsertRequestDto] bien rempli.
     * Postcondition : Rafraîchit l'information sur le serveur.
     */
    @PUT("reservant/{id}")
    suspend fun updateReservant(
        @Path("id") reservantId: Int,
        @Body request: ReservantUpsertRequestDto,
    ): ReservantDto

    /**
     * Rôle : Émet une requête de suppression (`DELETE`) sur un réservant ciblé.
     * 
     * Précondition : Le réservant cible de suppression.  
     * Postcondition : Nettoyage en base selon les règles serveur (ou soft delete côté API).
     */
    @DELETE("reservant/{id}")
    suspend fun deleteReservant(@Path("id") reservantId: Int): DeleteReservantResponseDto

    /**
     * Rôle : Demande au serveur quelles autres entités (contacts, réservations...) seront
     * impactées ou supprimées en cascade si on supprime ce réservant.
     * 
     * Précondition : ID du réservant.
     * Postcondition : Un état récapitulatif détaillé d'impact retourné par l'API ([ReservantDeleteSummaryDto]).
     */
    @GET("reservant/{id}/delete-summary")
    suspend fun getDeleteSummary(@Path("id") reservantId: Int): ReservantDeleteSummaryDto

    /**
     * Rôle : Sollicite la liste des contacts humains (Email/Téléphone) affiliés à ce réservant.
     * 
     * Précondition : Existence du réservant sur le serveur.
     * Postcondition : Liste structurée de [ReservantContactDto].
     */
    @GET("reservant/{id}/contacts")
    suspend fun getContacts(@Path("id") reservantId: Int): List<ReservantContactDto>

    /**
     * Rôle : Lie un tout nouveau contact physique défini au réservant porteur de cet ID.
     * 
     * Précondition : Les champs du form [ReservantContactUpsertRequestDto] sont valables.
     * Postcondition : Un nouveau point de contact est rattaché au réservant.
     */
    @POST("reservant/{id}/contacts")
    suspend fun addContact(
        @Path("id") reservantId: Int,
        @Body request: ReservantContactUpsertRequestDto,
    ): ReservantContactDto

    /**
     * Rôle : Demande tous les éditeurs globaux de la plateforme afin d'en assigner potentiellement un au réservant.
     * 
     * Précondition : Permssions de lecture du Catalogue.
     * Postcondition : Une liste de descriptions d'éditeurs ([ReservantEditorDto]).
     */
    @GET("editors")
    suspend fun getEditors(): List<ReservantEditorDto>
}
