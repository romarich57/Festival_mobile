package com.projetmobile.mobile.data.repository.games

import com.projetmobile.mobile.data.dao.GameDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.entity.games.PagedResult
import com.projetmobile.mobile.data.mapper.games.toEditorOption
import com.projetmobile.mobile.data.mapper.games.toGameDetail
import com.projetmobile.mobile.data.mapper.games.toGameListItem
import com.projetmobile.mobile.data.mapper.games.toGameRoomEntity
import com.projetmobile.mobile.data.mapper.games.toMechanismOption
import com.projetmobile.mobile.data.mapper.games.toGameTypeOption
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.games.GamesApiService
import com.projetmobile.mobile.data.remote.games.toRequestDto
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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.math.abs


class GamesRepositoryImpl(
    private val gamesApiService: GamesApiService,
    private val gameDao: GameDao,
    private val syncPreferenceStore: SyncPreferenceStore,
    private val syncScheduler: () -> Unit = { RepositorySyncScheduler.schedulePendingSyncAsync() },
) : GamesRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    override fun observeGames(titleSearch: String): Flow<List<com.projetmobile.mobile.data.entity.games.GameListItem>> =
        gameDao.observeByTitle(titleSearch).map { entities -> entities.map { it.toGameListItem() } }

    override fun observeGame(gameId: Int) =
        gameDao.observeById(gameId).map { it?.toGameDetail() }

    // ── Network-Bound Resource ───────────────────────────────────────────────

    override suspend fun refreshGames(
        filters: GameFilters,
        page: Int,
        limit: Int,
    ) = runRepositoryCall(defaultMessage = "Impossible de récupérer les jeux.") {
        val response = gamesApiService.getGames(
            page = page,
            limit = limit,
            title = filters.title.trim().takeIf { it.isNotEmpty() },
            type = filters.type?.trim()?.takeIf { it.isNotEmpty() },
            editorId = filters.editorId,
            minAge = filters.minAge,
            sort = filters.sort.apiValue,
        )
        val localById = gameDao.getAll().associateBy { it.id }
        val pageEntities = response.items.map { dto ->
            val remoteEntity = dto.toGameRoomEntity()
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
        val localOnlyEntities = localById.values.filter { entity ->
            shouldKeepLocalOnlyEntity(entity.id, entity.syncStatus, entity.retryAction)
        }
        val mergedEntities = (
            pageEntities +
                localOnlyEntities
            ).distinctBy { it.id }
        gameDao.upsertAll(mergedEntities)
        syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_GAMES)
        val visiblePageEntities = (
            pageEntities +
                localOnlyEntities.filter { entity -> entity.matches(filters) }
            ).distinctBy { it.id }
        PagedResult(
            items = visiblePageEntities
                .filterNot { entity ->
                    shouldHideFromCollections(entity.syncStatus, entity.retryAction)
                }
                .map { entity -> entity.toGameListItem() },
            page = response.pagination.page,
            limit = response.pagination.limit,
            total = response.pagination.total,
            hasNext = response.pagination.page < response.pagination.totalPages,
        )
    }

    override suspend fun getGame(gameId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer le jeu.",
    ) {
        val dto = gamesApiService.getGame(gameId)
        gameDao.upsert(dto.toGameRoomEntity())
        dto.toGameDetail()
    }

    // ── Écriture offline-first ───────────────────────────────────────────────

    override suspend fun createGame(draft: GameDraft) = runRepositoryCall(
        defaultMessage = "Impossible de créer le jeu.",
    ) {
        val localId = generateLocalId()
        val entity = draft.toGameRoomEntity(localId, SyncStatus.PENDING_CREATE)
        gameDao.upsert(entity)
        syncScheduler()
        entity.toGameDetail()
    }

    override suspend fun updateGame(gameId: Int, draft: GameDraft) = runRepositoryCall(
        defaultMessage = "Impossible de mettre à jour le jeu.",
    ) {
        val existing = gameDao.getById(gameId)
        if (existing != null) {
            val pendingJson = ApiJson.instance.encodeToString(GameDraft.serializer(), draft)
            val isPendingCreate = existing.id < 0 &&
                resolveRetryAction(existing.syncStatus, existing.retryAction) == SyncRetryAction.CREATE
            gameDao.upsert(
                existing.copy(
                    title = draft.title,
                    type = draft.type,
                    editorId = draft.editorId,
                    minAge = draft.minAge ?: existing.minAge,
                    authors = draft.authors,
                    minPlayers = draft.minPlayers,
                    maxPlayers = draft.maxPlayers,
                    prototype = draft.prototype,
                    durationMinutes = draft.durationMinutes,
                    theme = draft.theme,
                    description = draft.description,
                    imageUrl = draft.imageUrl,
                    rulesVideoUrl = draft.rulesVideoUrl,
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
            gameDao.getById(gameId)!!.toGameDetail()
        } else {
            // Item absent du cache → appel réseau direct
            val dto = gamesApiService.updateGame(gameId, draft.toRequestDto())
            gameDao.upsert(dto.toGameRoomEntity())
            dto.toGameDetail()
        }
    }

    override suspend fun deleteGame(gameId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de supprimer le jeu.",
    ) {
        val existing = gameDao.getById(gameId)
        when {
            existing == null -> {
                gamesApiService.deleteGame(gameId)
                gameDao.deleteById(gameId)
                "Jeu supprimé."
            }

            existing.id < 0 && existing.syncStatus == SyncStatus.PENDING_CREATE -> {
                gameDao.deleteById(existing.id)
                "Jeu supprimé localement."
            }

            else -> {
                gameDao.markForDeletion(existing.id)
                syncScheduler()
                "Suppression planifiée."
            }
        }
    }

    // ── Lookups ──────────────────────────────────────────────────────────────

    override suspend fun getGameTypes() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les types de jeux.",
    ) {
        gamesApiService.getGameTypes().map { it.toGameTypeOption() }
    }

    override suspend fun getEditors() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les éditeurs.",
    ) {
        gamesApiService.getEditors().map { it.toEditorOption() }
    }

    override suspend fun getMechanisms() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les mécanismes.",
    ) {
        gamesApiService.getMechanisms().map { it.toMechanismOption() }
    }

    override suspend fun uploadGameImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ) = runRepositoryCall(defaultMessage = "Impossible d'envoyer l'image du jeu.") {
        val requestBody = bytes.toRequestBody(mimeType.toMediaType())
        val imagePart = MultipartBody.Part.createFormData("image", fileName, requestBody)
        gamesApiService.uploadGameImage(imagePart).url
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun generateLocalId(): Int =
        -(abs(System.currentTimeMillis().toInt()).coerceAtLeast(1))

    private fun com.projetmobile.mobile.data.room.GameRoomEntity.matches(
        filters: GameFilters,
    ): Boolean {
        val normalizedTitle = filters.title.trim()
        val normalizedType = filters.type?.trim()
        return (normalizedTitle.isEmpty() || title.contains(normalizedTitle, ignoreCase = true)) &&
            (normalizedType.isNullOrEmpty() || type.equals(normalizedType, ignoreCase = true)) &&
            (filters.editorId == null || editorId == filters.editorId) &&
            (filters.minAge == null || minAge >= filters.minAge)
    }
}
