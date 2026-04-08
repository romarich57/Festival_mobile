package com.projetmobile.mobile.data.repository.festival

import com.projetmobile.mobile.data.dao.FestivalDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.mapper.festival.toFestivalRoomEntity
import com.projetmobile.mobile.data.mapper.festival.toFestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalApiService
import com.projetmobile.mobile.data.remote.festival.FestivalDto
import com.projetmobile.mobile.data.repository.runRepositoryCall
import com.projetmobile.mobile.data.sync.RepositorySyncScheduler
import com.projetmobile.mobile.data.sync.shouldHideFromCollections
import com.projetmobile.mobile.data.sync.shouldPreserveLocalDuringRefresh
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FestivalRepositoryImpl(
    private val festivalApiService: FestivalApiService,
    private val festivalDao: FestivalDao,
    private val syncPreferenceStore: SyncPreferenceStore,
    private val syncScheduler: () -> Unit = { RepositorySyncScheduler.schedulePendingSyncAsync() },
) : FestivalRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    override fun observeFestivals(): Flow<List<com.projetmobile.mobile.data.entity.festival.FestivalSummary>> =
        festivalDao.observeAll().map { entities -> entities.map { it.toFestivalSummary() } }

    override fun observeFestival(id: Int) =
        festivalDao.observeById(id).map { it?.toFestivalSummary() }

    // ── Network-Bound Resource ───────────────────────────────────────────────

    override suspend fun refreshFestivals() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les festivals.",
    ) {
        val dtos = festivalApiService.getFestivals()
        val localById = festivalDao.getAll().associateBy { it.id }
        val remoteIds = mutableSetOf<Int>()
        val mergedEntities = dtos.map { dto ->
            val remoteEntity = dto.toFestivalRoomEntity()
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
        festivalDao.upsertAll(mergedEntities)
        localById.values
            .filter { entity ->
                entity.id > 0 &&
                    entity.id !in remoteIds &&
                    !shouldPreserveLocalDuringRefresh(entity.syncStatus, entity.retryAction)
            }
            .forEach { entity ->
                festivalDao.deleteById(entity.id)
            }
        syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_FESTIVALS)
        mergedEntities
            .filterNot { entity ->
                shouldHideFromCollections(entity.syncStatus, entity.retryAction)
            }
            .map { entity -> entity.toFestivalSummary() }
    }

    override suspend fun getFestival(id: Int) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer le festival.",
    ) {
        val dto = festivalApiService.getFestival(id)
        val entity = dto.toFestivalRoomEntity()
        festivalDao.upsert(entity)
        entity.toFestivalSummary()
    }

    // ── Admin (réseau direct) ────────────────────────────────────────────────

    override suspend fun addFestival(festival: FestivalDto) = runRepositoryCall(
        defaultMessage = "Impossible d'ajouter le festival.",
    ) {
        val response = festivalApiService.addFestival(festival)
        val created = response.festival
        val canonical = created.id?.let { createdId ->
            festivalApiService.getFestival(createdId)
        }

        if (canonical != null) {
            festivalDao.upsert(canonical.toFestivalRoomEntity())
            syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_FESTIVALS)
            canonical
        } else {
            val festivals = festivalApiService.getFestivals()
            festivalDao.upsertAll(festivals.map { it.toFestivalRoomEntity() })
            syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_FESTIVALS)
            festivals.firstOrNull { dto ->
                dto.name == festival.name.trim() &&
                    dto.startDate == festival.startDate &&
                    dto.endDate == festival.endDate
            } ?: created
        }
    }

    override suspend fun deleteFestival(id: Int) = runRepositoryCall(
        defaultMessage = "Impossible de supprimer le festival.",
    ) {
        val existing = festivalDao.getById(id)
        if (existing == null) {
            festivalApiService.deleteFestival(id)
            festivalDao.deleteById(id)
        } else {
            festivalDao.markForDeletion(existing.id)
            syncScheduler()
        }
    }
}
