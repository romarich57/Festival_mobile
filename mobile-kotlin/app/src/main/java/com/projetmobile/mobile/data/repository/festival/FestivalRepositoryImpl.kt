package com.projetmobile.mobile.data.repository.festival

import com.projetmobile.mobile.data.dao.FestivalDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.mapper.festival.toFestivalRoomEntity
import com.projetmobile.mobile.data.mapper.festival.toFestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalApiService
import com.projetmobile.mobile.data.remote.festival.FestivalDto
import com.projetmobile.mobile.data.repository.runRepositoryCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FestivalRepositoryImpl(
    private val festivalApiService: FestivalApiService,
    private val festivalDao: FestivalDao,
    private val syncPreferenceStore: SyncPreferenceStore,
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
        festivalDao.upsertAll(dtos.map { it.toFestivalRoomEntity() })
        syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_FESTIVALS)
        dtos.map { it.toFestivalRoomEntity().toFestivalSummary() }
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
        val created = festivalApiService.addFestival(festival)
        festivalDao.upsert(created.toFestivalRoomEntity())
        created
    }

    override suspend fun deleteFestival(id: Int) = runRepositoryCall(
        defaultMessage = "Impossible de supprimer le festival.",
    ) {
        festivalApiService.deleteFestival(id)
        // Suppression dans Room après confirmation serveur
    }
}
