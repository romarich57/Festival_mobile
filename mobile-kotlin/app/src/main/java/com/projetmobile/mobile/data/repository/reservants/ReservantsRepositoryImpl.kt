package com.projetmobile.mobile.data.repository.reservants

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.projetmobile.mobile.data.dao.ReservantDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.mapper.reservants.toReservantContact
import com.projetmobile.mobile.data.mapper.reservants.toReservantDeleteSummary
import com.projetmobile.mobile.data.mapper.reservants.toReservantDetail
import com.projetmobile.mobile.data.mapper.reservants.toReservantEditorOption
import com.projetmobile.mobile.data.mapper.reservants.toReservantListItem
import com.projetmobile.mobile.data.mapper.reservants.toReservantRoomEntity
import com.projetmobile.mobile.data.remote.reservants.ReservantsApiService
import com.projetmobile.mobile.data.remote.reservants.toRequestDto
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.repository.runRepositoryCall
import com.projetmobile.mobile.data.room.SyncStatus
import com.projetmobile.mobile.data.worker.ReservantSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class ReservantsRepositoryImpl(
    private val reservantsApiService: ReservantsApiService,
    private val reservantDao: ReservantDao,
    private val syncPreferenceStore: SyncPreferenceStore,
    private val context: Context,
    private val syncScheduler: () -> Unit = { enqueueReservantSync(context) },
) : ReservantsRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    override fun observeReservants(): Flow<List<com.projetmobile.mobile.data.entity.reservants.ReservantListItem>> =
        reservantDao.observeAll().map { entities -> entities.map { it.toReservantListItem() } }

    override fun observeReservant(reservantId: Int) =
        reservantDao.observeById(reservantId).map { it?.toReservantDetail() }

    // ── Network-Bound Resource ───────────────────────────────────────────────

    override suspend fun refreshReservants() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les réservants.",
    ) {
        val dtos = reservantsApiService.getReservants()
        reservantDao.upsertAll(dtos.map { it.toReservantRoomEntity() })
        syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_RESERVANTS)
        dtos.map { it.toReservantListItem() }
    }

    override suspend fun getReservant(reservantId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer le réservant.",
    ) {
        val dto = reservantsApiService.getReservant(reservantId)
        reservantDao.upsert(dto.toReservantRoomEntity())
        dto.toReservantDetail()
    }

    // ── Écriture offline-first ───────────────────────────────────────────────

    override suspend fun createReservant(draft: ReservantDraft) = runRepositoryCall(
        defaultMessage = "Impossible de créer le réservant.",
    ) {
        val localId = generateLocalId()
        val entity = draft.toReservantRoomEntity(localId, SyncStatus.PENDING_CREATE)
        reservantDao.upsert(entity)
        syncScheduler()
        entity.toReservantDetail()
    }

    override suspend fun updateReservant(reservantId: Int, draft: ReservantDraft) =
        runRepositoryCall(defaultMessage = "Impossible de mettre à jour le réservant.") {
            val existing = reservantDao.getById(reservantId)
            if (existing != null) {
                val pendingJson = ApiJson.instance.encodeToString(ReservantDraft.serializer(), draft)
                reservantDao.upsert(
                    existing.copy(
                        name = draft.name,
                        email = draft.email,
                        type = draft.type,
                        editorId = draft.editorId,
                        phoneNumber = draft.phoneNumber,
                        address = draft.address,
                        siret = draft.siret,
                        notes = draft.notes,
                        syncStatus = SyncStatus.PENDING_UPDATE,
                        pendingDraftJson = pendingJson,
                    ),
                )
                syncScheduler()
                reservantDao.getById(reservantId)!!.toReservantDetail()
            } else {
                val dto = reservantsApiService.updateReservant(reservantId, draft.toRequestDto())
                reservantDao.upsert(dto.toReservantRoomEntity())
                dto.toReservantDetail()
            }
        }

    override suspend fun getDeleteSummary(reservantId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de charger le résumé de suppression du réservant.",
    ) {
        reservantsApiService.getDeleteSummary(reservantId).toReservantDeleteSummary()
    }

    override suspend fun deleteReservant(reservantId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de supprimer le réservant.",
    ) {
        val existing = reservantDao.getById(reservantId)
        when {
            existing == null -> "Réservant introuvable."

            existing.id < 0 && existing.syncStatus == SyncStatus.PENDING_CREATE -> {
                reservantDao.deleteById(existing.id)
                "Réservant supprimé localement."
            }

            else -> {
                reservantDao.markForDeletion(existing.id)
                syncScheduler()
                "Suppression planifiée."
            }
        }
    }

    // ── Contacts & Lookups (réseau direct) ──────────────────────────────────

    override suspend fun getEditors() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les éditeurs.",
    ) {
        reservantsApiService.getEditors().map { it.toReservantEditorOption() }
    }

    override suspend fun getContacts(reservantId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les contacts du réservant.",
    ) {
        reservantsApiService.getContacts(reservantId).map { it.toReservantContact() }
    }

    override suspend fun addContact(
        reservantId: Int,
        draft: ReservantContactDraft,
    ) = runRepositoryCall(defaultMessage = "Impossible d'ajouter le contact.") {
        reservantsApiService.addContact(reservantId, draft.toRequestDto()).toReservantContact()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun generateLocalId(): Int =
        -(abs(System.currentTimeMillis().toInt()).coerceAtLeast(1))

    companion object {
        private fun enqueueReservantSync(context: Context) {
            val request = OneTimeWorkRequestBuilder<ReservantSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                ReservantSyncWorker.WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }
    }
}
