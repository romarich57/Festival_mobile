package com.projetmobile.mobile.data.worker

import android.content.Context
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.mapper.games.toGameRoomEntity
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.FestivalApplication
import com.projetmobile.mobile.data.remote.games.toRequestDto
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import androidx.work.WorkerParameters
import androidx.work.CoroutineWorker
import com.projetmobile.mobile.data.sync.resolveRetryAction
import kotlin.math.abs

/**
 * Worker chargé de synchroniser les jeux en attente avec le serveur.
 */
class GameSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "game_sync"
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as FestivalApplication
        val container = app.appContainer
        val gameDao = AppDatabase.getInstance(applicationContext).gameDao()
        val api = container.gamesApiService

        val pending = gameDao.getPending()
        if (pending.isEmpty()) return Result.success()

        var hasRetryableError = false

        for (entity in pending) {
            val action = resolveRetryAction(entity.syncStatus, entity.retryAction) ?: continue
            try {
                when (action) {
                    SyncRetryAction.CREATE -> {
                        val draft = ApiJson.instance.decodeFromString(
                            GameDraft.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        val serverDto = api.createGame(draft.toRequestDto())
                        gameDao.deleteById(entity.id)
                        gameDao.upsert(serverDto.toGameRoomEntity(SyncStatus.SYNCED))
                    }

                    SyncRetryAction.UPDATE -> {
                        val draft = ApiJson.instance.decodeFromString(
                            GameDraft.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        val serverId = abs(entity.id)
                        val serverDto = api.updateGame(serverId, draft.toRequestDto())
                        gameDao.upsert(serverDto.toGameRoomEntity(SyncStatus.SYNCED))
                    }

                    SyncRetryAction.DELETE -> {
                        if (entity.id > 0) {
                            api.deleteGame(abs(entity.id))
                        }
                        gameDao.deleteById(entity.id)
                    }
                }
            } catch (throwable: Throwable) {
                if (action == SyncRetryAction.DELETE && throwable.isDeleteAlreadyApplied()) {
                    gameDao.deleteById(entity.id)
                    continue
                }

                val retryable = throwable.isRetryableSyncFailure("Impossible de synchroniser le jeu.")
                gameDao.updateSyncState(
                    id = entity.id,
                    status = SyncStatus.ERROR,
                    retryAction = if (retryable) action else null,
                    lastSyncErrorMessage = throwable.toSyncFailureMessage(
                        "Impossible de synchroniser le jeu.",
                    ),
                )
                if (retryable) {
                    hasRetryableError = true
                }
            }
        }

        return if (hasRetryableError) Result.retry() else Result.success()
    }
}
