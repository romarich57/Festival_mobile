package com.projetmobile.mobile.data.repository.reservation

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.projetmobile.mobile.data.dao.ReservationDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.mapper.reservation.toReservationDashboardRow
import com.projetmobile.mobile.data.mapper.reservation.toReservationRoomEntity
import com.projetmobile.mobile.data.remote.reservation.ReservationApiService
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ReservationDetailsDto
import com.projetmobile.mobile.data.remote.reservation.ReservationUpdatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ZoneTarifaireDto
import com.projetmobile.mobile.data.repository.runRepositoryCall
import com.projetmobile.mobile.data.room.SyncStatus
import com.projetmobile.mobile.data.worker.ReservationSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class ReservationRepositoryImpl(
    private val api: ReservationApiService,
    private val reservationDao: ReservationDao,
    private val syncPreferenceStore: SyncPreferenceStore,
    private val context: Context,
) : ReservationRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    override fun observeReservations(festivalId: Int): Flow<List<ReservationDashboardRowEntity>> =
        reservationDao.observeByFestival(festivalId)
            .map { entities -> entities.map { it.toReservationDashboardRow() } }

    // ── Network-Bound Resource ───────────────────────────────────────────────

    override suspend fun refreshReservations(festivalId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les réservations.",
    ) {
        val dtos = api.getReservationsByFestival(festivalId)
        reservationDao.upsertAll(dtos.map { it.toReservationRoomEntity(festivalId) })
        syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_RESERVATIONS)
        dtos.map { it.toReservationRoomEntity(festivalId).toReservationDashboardRow() }
    }

    // ── Écriture offline-first ───────────────────────────────────────────────

    override suspend fun createReservation(payload: ReservationCreatePayloadDto) =
        runRepositoryCall(defaultMessage = "Impossible de créer la réservation.") {
            val localId = generateLocalId()
            val entity = payload.toReservationRoomEntity(localId)
            reservationDao.upsert(entity)
            scheduleSync()
        }

    override suspend fun deleteReservation(reservationId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de supprimer la réservation.",
    ) {
        reservationDao.markForDeletion(reservationId)
        scheduleSync()
    }

    // ── Opérations réseau directes ───────────────────────────────────────────

    override suspend fun getReservationDetails(reservationId: Int): ReservationDetailsDto {
        return api.getReservationDetails(reservationId)
    }

    override suspend fun updateReservation(
        reservationId: Int,
        payload: ReservationUpdatePayloadDto,
    ) {
        api.updateReservation(reservationId, payload)
        val entity = reservationDao.getById(reservationId)
        if (entity != null) {
            val dtos = api.getReservationsByFestival(entity.festivalId)
            reservationDao.upsertAll(dtos.map { it.toReservationRoomEntity(entity.festivalId) })
        }
        syncPreferenceStore.invalidate(SyncPreferenceStore.KEY_RESERVATIONS)
    }

    override suspend fun getZonesTarifaires(festivalId: Int): List<ZoneTarifaireDto> {
        return api.getZonesTarifaires(festivalId)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun generateLocalId(): Int =
        -(abs(System.currentTimeMillis().toInt()).coerceAtLeast(1))

    private fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<ReservationSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ReservationSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
