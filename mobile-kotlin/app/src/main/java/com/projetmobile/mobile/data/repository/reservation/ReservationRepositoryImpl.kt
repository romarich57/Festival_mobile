package com.projetmobile.mobile.data.repository.reservation

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
import com.projetmobile.mobile.data.sync.RepositorySyncScheduler
import com.projetmobile.mobile.data.sync.shouldHideFromCollections
import com.projetmobile.mobile.data.sync.shouldKeepLocalOnlyEntity
import com.projetmobile.mobile.data.sync.shouldPreserveLocalDuringRefresh
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.abs

/**
 * Rôle : Implémentation complexe SSOT (Single Source Of Truth) du repository [ReservationRepository].
 * Mixe l'exploitation Offline-First pour la création/suppression et les vues tableaux avec des fetch Directs 
 * pour les détails de facturation (lourds), trop complexes pour être gardés en Base de Données Room. 
 * 
 * Précondition : [ReservationDao] et [ReservationApiService] opérationnels.
 * Postcondition : Délivre ce comportement hybride dans des suspend functions sans que le composant appelant n'en soit conscient.
 */
class ReservationRepositoryImpl(
    private val api: ReservationApiService,
    private val reservationDao: ReservationDao,
    private val syncPreferenceStore: SyncPreferenceStore,
    private val syncScheduler: () -> Unit = { RepositorySyncScheduler.schedulePendingSyncAsync() },
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
        val localById = reservationDao.getAllByFestival(festivalId).associateBy { it.id }
        val remoteIds = mutableSetOf<Int>()
        val mergedEntities = dtos.map { dto ->
            val remoteEntity = dto.toReservationRoomEntity(festivalId)
            remoteIds += remoteEntity.id
            val localEntity = localById[remoteEntity.id]
            if (
                localEntity != null &&
                shouldPreserveLocalDuringRefresh(localEntity.syncStatus, localEntity.retryAction)
            ) {
                localEntity
            } else {
                remoteEntity
            }
        }
        reservationDao.upsertAll(mergedEntities)
        localById.values
            .filter { entity ->
                entity.id > 0 &&
                    entity.id !in remoteIds &&
                    !shouldPreserveLocalDuringRefresh(entity.syncStatus, entity.retryAction)
            }
            .forEach { entity ->
                reservationDao.deleteById(entity.id)
            }
        syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_RESERVATIONS)
        (
            mergedEntities +
                localById.values.filter { entity ->
                    shouldKeepLocalOnlyEntity(entity.id, entity.syncStatus, entity.retryAction)
                }
            ).distinctBy { it.id }
            .filterNot { entity ->
                shouldHideFromCollections(entity.syncStatus, entity.retryAction)
            }
            .map { entity -> entity.toReservationDashboardRow() }
    }

    // ── Écriture offline-first ───────────────────────────────────────────────

    override suspend fun createReservation(payload: ReservationCreatePayloadDto) =
        runRepositoryCall(defaultMessage = "Impossible de créer la réservation.") {
            val localId = generateLocalId()
            val entity = payload.toReservationRoomEntity(localId)
            reservationDao.upsert(entity)
            syncScheduler()
        }

    override suspend fun deleteReservation(reservationId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de supprimer la réservation.",
    ) {
        val existing = reservationDao.getById(reservationId)
        when {
            existing == null -> Unit

            existing.id < 0 && existing.syncStatus == SyncStatus.PENDING_CREATE -> {
                reservationDao.deleteById(existing.id)
            }

            else -> {
                reservationDao.markForDeletion(existing.id)
                syncScheduler()
            }
        }
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
            val localById = reservationDao.getAllByFestival(entity.festivalId).associateBy { it.id }
            val remoteIds = mutableSetOf<Int>()
            val mergedEntities = dtos.map { dto ->
                val remoteEntity = dto.toReservationRoomEntity(entity.festivalId)
                remoteIds += remoteEntity.id
                val localEntity = localById[remoteEntity.id]
                if (
                    localEntity != null &&
                    shouldPreserveLocalDuringRefresh(localEntity.syncStatus, localEntity.retryAction)
                ) {
                    localEntity
                } else {
                    remoteEntity
                }
            }
            reservationDao.upsertAll(mergedEntities)
            localById.values
                .filter { localEntity ->
                    localEntity.id > 0 &&
                        localEntity.id !in remoteIds &&
                        !shouldPreserveLocalDuringRefresh(
                            localEntity.syncStatus,
                            localEntity.retryAction,
                        )
                }
                .forEach { localEntity ->
                    reservationDao.deleteById(localEntity.id)
                }
        }
        syncPreferenceStore.invalidate(SyncPreferenceStore.KEY_RESERVATIONS)
    }

    override suspend fun getZonesTarifaires(festivalId: Int): List<ZoneTarifaireDto> {
        return api.getZonesTarifaires(festivalId)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun generateLocalId(): Int =
        -(abs(System.currentTimeMillis().toInt()).coerceAtLeast(1))
}
