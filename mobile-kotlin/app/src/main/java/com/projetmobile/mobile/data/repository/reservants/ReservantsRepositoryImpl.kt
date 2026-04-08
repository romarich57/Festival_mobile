package com.projetmobile.mobile.data.repository.reservants

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
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import com.projetmobile.mobile.data.sync.RepositorySyncScheduler
import com.projetmobile.mobile.data.sync.resolveRetryAction
import com.projetmobile.mobile.data.sync.shouldHideFromCollections
import com.projetmobile.mobile.data.sync.shouldKeepLocalOnlyEntity
import com.projetmobile.mobile.data.sync.shouldPreserveLocalDuringRefresh
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.abs

/**
 * Rôle : Implémentation Offline-First du Repository Gérant les Réservants.
 * Réduit le couple réseau/BDD locale par un principe d’Upsert conditionné selon les états de synchro
 * (PENDING_CREATE, DELETE).
 * 
 * Précondition : Appels orchestrables en background et API configurée.
 * Postcondition : Maintenabilité du Mode Hors-Ligne pour les utilisateurs via le Flow SSOT (Source unique de vérité).
 */
class ReservantsRepositoryImpl(
    private val reservantsApiService: ReservantsApiService,
    private val reservantDao: ReservantDao,
    private val syncPreferenceStore: SyncPreferenceStore,
    private val syncScheduler: () -> Unit = { RepositorySyncScheduler.schedulePendingSyncAsync() },
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
        val localById = reservantDao.getAll().associateBy { it.id }
        val remoteIds = mutableSetOf<Int>()
        val mergedEntities = dtos.map { dto ->
            val remoteEntity = dto.toReservantRoomEntity()
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
        reservantDao.upsertAll(mergedEntities)
        localById.values
            .filter { entity ->
                entity.id > 0 &&
                    entity.id !in remoteIds &&
                    !shouldPreserveLocalDuringRefresh(entity.syncStatus, entity.retryAction)
            }
            .forEach { entity ->
                reservantDao.deleteById(entity.id)
            }
        syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_RESERVANTS)
        (
            mergedEntities +
                localById.values.filter { entity ->
                    shouldKeepLocalOnlyEntity(entity.id, entity.syncStatus, entity.retryAction)
                }
            ).distinctBy { it.id }
            .filterNot { entity ->
                shouldHideFromCollections(entity.syncStatus, entity.retryAction)
            }
            .map { entity -> entity.toReservantListItem() }
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
                val isPendingCreate = existing.id < 0 &&
                    resolveRetryAction(existing.syncStatus, existing.retryAction) == SyncRetryAction.CREATE
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
                        syncStatus = if (isPendingCreate) {
                            SyncStatus.PENDING_CREATE
                        } else {
                            SyncStatus.PENDING_UPDATE
                        },
                        pendingDraftJson = pendingJson,
                        retryAction = if (isPendingCreate) {
                            SyncRetryAction.CREATE
                        } else {
                            SyncRetryAction.UPDATE
                        },
                        lastSyncErrorMessage = null,
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
}
