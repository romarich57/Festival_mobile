package com.projetmobile.mobile.data.remote.reservation

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Rôle : Définit l'ensemble des appels API Retrofit utilisés pour gérer les 
 * réservations d'un festival, incluant les états administratifs (workflows), 
 * la tarification, et le suivi relationnel.
 * 
 * Précondition : La configuration Retrofit (avec AuthInterceptor) doit être valide.
 * Postcondition : Offre les suspend functions pour effectuer l'ensemble des opérations du module de réservation.
 */
interface ReservationApiService {
    
    /**
     * Rôle : Interroge le serveur pour récupérer synthétiquement toutes 
     * les réservations relatives à un ID de festival précis.
     * 
     * Précondition : Le festival ciblé par `festivalId` existe.
     * Postcondition : Fournit un tableau condensé de lignes ([ReservationDashboardRowDto]) pour les tableaux d'administration.
     */
    @GET("reservation/reservations/{festivalId}")
    suspend fun getReservationsByFestival(
        @Path("festivalId") festivalId: Int
    ): List<ReservationDashboardRowDto>

    /**
     * Rôle : Valide une nouvelle demande de réservation en base de données distante.
     * 
     * Précondition : Payload complet avec identité réservant, ID du festival, et estimations.
     * Postcondition : Le backend génère le contrat de réservation (y compris le workflow).
     */
    @POST("reservation/reservation")
    suspend fun createReservation(@Body payload: ReservationCreatePayloadDto)

    /**
     * Rôle : Demande la destruction complète de l'entité Réservation.
     * 
     * Précondition : L'autorisation administrative est requise.
     * Postcondition : La réservation (et possiblement son workflow / ses tables) est effacée.
     */
    @DELETE("reservation/reservation/{id}")
    suspend fun deleteReservation(@Path("id") id: Int)

    /**
     * Rôle : Rapport ciblé sur les états relationnels (Workflow) validés ou non d'une réservation 
     * à un instant T.
     * 
     * Précondition : Associer un `reservationId` valide.
     * Postcondition : Retourne la liste d'étapes (contact, liste de jeux validée...).
     */
    @GET("workflow/reservation/{reservationId}")
    suspend fun getWorkflowByReservationId(
        @Path("reservationId") reservationId: Int
    ): WorkflowDto

    /**
     * Rôle : Met à jour les cases cochées de suivi administratif d'un Workflow distant.
     * 
     * Précondition : Un ID de Workflow (attention, pas un ID de réservation) et le mapping des booléens.
     * Postcondition : Renvoie le Workflow actualisé des nouvelles cases.
     */
    // PUT /workflow/:id
    @PUT("workflow/{id}")
    suspend fun updateWorkflow(
        @Path("id") id: Int,
        @Body workflowData: WorkflowUpdatePayload
    ): WorkflowDto

    /**
     * Rôle : Horodate une relance téléphonique ou email associée à ce Workflow.
     * 
     * Précondition : ID du Workflow à relancer.
     * Postcondition : Le backend rajoute "Aujourd'hui" à son tableau `contact_dates`.
     */
    // POST /workflow/:id/contact
    @POST("workflow/{id}/contact")
    suspend fun addContactDate(
        @Path("id") id: Int
    ): List<String>

    /**
     * Rôle : Apporte la vue profonde administrative (facturation, etc.) d'une réservation ciblée.
     * 
     * Précondition : Réservation établie côté serveur.
     * Postcondition : Objet structuré complet ([ReservationDetailsDto]) incluant les données monétaires.
     */
    @GET("reservation/detail/{reservationId}")
    suspend fun getReservationDetails(
        @Path("reservationId") reservationId: Int
    ): ReservationDetailsDto

    /**
     * Rôle : Alterne les valeurs chiffrées modifiables du bordereau de réservation 
     * (remises, prix final, notes).
     * 
     * Précondition : L'ID formel de la réservation et les modifications voulues.
     * Postcondition : Synchronise ces nouvelles décisions côté SQL serveur.
     */
    @PUT("reservation/reservation/{id}")
    suspend fun updateReservation(
        @Path("id") id: Int,
        @Body payload: ReservationUpdatePayloadDto
    )

    /**
     * Rôle : Aide contextuelle ramenant toutes les tarifications (espaces) débloquées et configurées
     * pour le festival.
     * 
     * Précondition : Festival initialisé avec ses sous-zones tarifaires.
     * Postcondition : Un répertoire de tarifs et capacités disponibles [ZoneTarifaireDto].
     */
    @GET("zones-tarifaires/{festivalId}")
    suspend fun getZonesTarifaires(
        @Path("festivalId") festivalId: Int
    ): List<ZoneTarifaireDto>
}
