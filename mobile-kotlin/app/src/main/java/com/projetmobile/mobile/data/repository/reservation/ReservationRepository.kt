package com.projetmobile.mobile.data.repository.reservation

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ReservationDetailsDto
import com.projetmobile.mobile.data.remote.reservation.ReservationUpdatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ZoneTarifaireDto
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    /** Flux live des réservations d'un festival depuis Room. */
    fun observeReservations(festivalId: Int): Flow<List<ReservationDashboardRowEntity>>

    // ── Déclenchement réseau ────────────────────────────────────────────────

    /** Rafraîchit les réservations d'un festival depuis le réseau et met à jour Room. */
    suspend fun refreshReservations(festivalId: Int): Result<List<ReservationDashboardRowEntity>>

    // ── Écriture offline-first ───────────────────────────────────────────────

    /** Crée une réservation hors-ligne : Room immédiat + WorkManager si connecté. */
    suspend fun createReservation(payload: ReservationCreatePayloadDto): Result<Unit>

    /** Supprime une réservation hors-ligne : PENDING_DELETE dans Room + WorkManager. */
    suspend fun deleteReservation(reservationId: Int): Result<Unit>

    // ── Opérations réseau directes (non offline-first) ───────────────────────

    suspend fun getReservationDetails(reservationId: Int): ReservationDetailsDto

    suspend fun updateReservation(
        reservationId: Int,
        payload: ReservationUpdatePayloadDto,
    )

    suspend fun getZonesTarifaires(festivalId: Int): List<ZoneTarifaireDto>
}
