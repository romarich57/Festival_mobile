package com.projetmobile.mobile.data.repository.games

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.projetmobile.mobile.data.dao.GameDao
import com.projetmobile.mobile.data.database.SyncPreferenceStore
import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.entity.games.GameFilters
import com.projetmobile.mobile.data.mapper.games.toEditorOption
import com.projetmobile.mobile.data.mapper.games.toGameDetail
import com.projetmobile.mobile.data.mapper.games.toGameListItem
import com.projetmobile.mobile.data.mapper.games.toGameListPage
import com.projetmobile.mobile.data.mapper.games.toGameRoomEntity
import com.projetmobile.mobile.data.mapper.games.toMechanismOption
import com.projetmobile.mobile.data.mapper.games.toGameTypeOption
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.games.GamesApiService
import com.projetmobile.mobile.data.remote.games.toRequestDto
import com.projetmobile.mobile.data.repository.runRepositoryCall
import com.projetmobile.mobile.data.room.SyncStatus
import com.projetmobile.mobile.data.worker.GameSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class GamesRepositoryImpl(
    private val gamesApiService: GamesApiService,
    private val gameDao: GameDao,
    private val syncPreferenceStore: SyncPreferenceStore,
    private val context: Context,
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
        gameDao.upsertAll(response.items.map { it.toGameRoomEntity() })
        syncPreferenceStore.setLastSyncedAt(SyncPreferenceStore.KEY_GAMES)
        response.toGameListPage()
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
        scheduleSync()
        entity.toGameDetail()
    }

    override suspend fun updateGame(gameId: Int, draft: GameDraft) = runRepositoryCall(
        defaultMessage = "Impossible de mettre à jour le jeu.",
    ) {
        val existing = gameDao.getById(gameId)
        if (existing != null) {
            val pendingJson = ApiJson.instance.encodeToString(GameDraft.serializer(), draft)
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
                    syncStatus = SyncStatus.PENDING_UPDATE,
                    pendingDraftJson = pendingJson,
                ),
            )
            scheduleSync()
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
        gameDao.markForDeletion(gameId)
        scheduleSync()
        "Suppression planifiée."
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

    private fun scheduleSync() {
        val request = OneTimeWorkRequestBuilder<GameSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build(),
            )
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            GameSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request,
        )
    }
}
