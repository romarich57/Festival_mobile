package com.projetmobile.mobile.data.repository.reservation

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ReservationDetailsDto
import com.projetmobile.mobile.data.remote.reservation.ReservationUpdatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ZoneTarifaireDto
import kotlinx.coroutines.flow.Flow

/**
 * Rôle : Assure le management des instances de "Réservations" 
 * (l'assignation d'un espace de festival à un Réservant). 
 * Fait la jonction entre l'affichage liste (SSOT Room) et l'édition fine (Réseau Direct).
 * 
 * Précondition : Initialisation du Dagger/Koin pour l'abstraction Repository.
 * Postcondition : Un contrat strict pour les requêtes asynchrones concernant l'espace d'administration du festival.
 */
interface ReservationRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    /**
     * Rôle : Obtenir la collection réactive totale de réservations associées à un festival.
     * 
     * Précondition : Fournir l'ID courant du festival sélectionné.
     * Postcondition : Un Flux listant les objets préformatés pour le tableau de bord [ReservationDashboardRowEntity].
     */
    fun observeReservations(festivalId: Int): Flow<List<ReservationDashboardRowEntity>>

    // ── Déclenchement réseau ────────────────────────────────────────────────

    /**
     * Rôle : Demander un téléchargement des réservations du festival depuis le back-end et enrichir la base locale.
     * 
     * Précondition : L'index `festivalId` doit correspondre à une édition existante.
     * Postcondition : Opération d'Upsert et expurgation des réservations locales mortes.
     */
    suspend fun refreshReservations(festivalId: Int): Result<List<ReservationDashboardRowEntity>>

    // ── Écriture offline-first ───────────────────────────────────────────────

    /**
     * Rôle : Déposer une demande de nouvelle réservation de façon déconnectée (Offline-First).
     * 
     * Précondition : Formulaire DTO complet [ReservationCreatePayloadDto].
     * Postcondition : Écriture dans Room à l'état `PENDING_CREATE` ; enrôlement d'un WorkManager en tâche de fond.
     */
    suspend fun createReservation(payload: ReservationCreatePayloadDto): Result<Unit>

    /**
     * Rôle : Rompre un contrat de réservation, géré hors-ligne.
     * 
     * Précondition : La cible à supprimer `reservationId`.
     * Postcondition : Disparition silencieuse en local (PENDING_DELETE) jusqu'à résolution via internet.
     */
    suspend fun deleteReservation(reservationId: Int): Result<Unit>

    // ── Opérations réseau directes (non offline-first) ───────────────────────

    /**
     * Rôle : Consulter le volet complet "Détails" (calculs financiers, etc.) sur le serveur de manière synchrone.
     * 
     * Précondition : L'identifiant de la réservation validé par l'API DB.
     * Postcondition : Renvoie un grand modèle [ReservationDetailsDto].
     */
    suspend fun getReservationDetails(reservationId: Int): ReservationDetailsDto

    /**
     * Rôle : Transmettre une mutation importante (ajustement de tables, remises tarifaires).
     * 
     * Précondition : Fournir l'ID et l'historique complet modifié via [ReservationUpdatePayloadDto].
     * Postcondition : Soumission acceptée côté Serveur, puis trigger rafraîchissement local.
     */
    suspend fun updateReservation(
        reservationId: Int,
        payload: ReservationUpdatePayloadDto,
    )

    /**
     * Rôle : Lister en cache distant quelles sont les zones (plans de sol) disponibles et leurs montants.
     * 
     * Précondition : Un `festivalId` ciblé.
     * Postcondition : Catalogue numéraire retourné ([ZoneTarifaireDto]).
     */
    suspend fun getZonesTarifaires(festivalId: Int): List<ZoneTarifaireDto>
}
