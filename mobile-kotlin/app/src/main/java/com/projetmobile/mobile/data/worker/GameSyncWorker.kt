package com.projetmobile.mobile.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.mapper.games.toGameRoomEntity
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.games.GamesApiService
import com.projetmobile.mobile.data.remote.games.toRequestDto
import com.projetmobile.mobile.data.room.SyncStatus
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.projetmobile.mobile.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import kotlin.math.abs

/**
 * Worker chargé de synchroniser les jeux en attente (PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE)
 * avec le serveur, lorsque la connexion réseau est disponible.
 *
 * Planifié via WorkManager avec [ExistingWorkPolicy.KEEP] pour éviter les doublons.
 * En cas d'échec, le statut passe à ERROR et WorkManager retente avec backoff exponentiel.
 */
class GameSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "game_sync"
    }

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val gameDao = db.gameDao()

        // Construction minimale de Retrofit (cookies gérés par l'appli principale)
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(
                ApiJson.instance.asConverterFactory("application/json".toMediaType()),
            )
            .build()
        val api = retrofit.create(GamesApiService::class.java)

        val pending = gameDao.getPending()
        if (pending.isEmpty()) return Result.success()

        var hasError = false

        for (entity in pending) {
            try {
                when (entity.syncStatus) {
                    SyncStatus.PENDING_CREATE -> {
                        val draft = ApiJson.instance.decodeFromString(
                            GameDraft.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        val serverDto = api.createGame(draft.toRequestDto())
                        gameDao.deleteById(entity.id)
                        gameDao.upsert(serverDto.toGameRoomEntity(SyncStatus.SYNCED))
                    }

                    SyncStatus.PENDING_UPDATE -> {
                        val draft = ApiJson.instance.decodeFromString(
                            GameDraft.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        val serverId = abs(entity.id)
                        val serverDto = api.updateGame(serverId, draft.toRequestDto())
                        gameDao.upsert(serverDto.toGameRoomEntity(SyncStatus.SYNCED))
                    }

                    SyncStatus.PENDING_DELETE -> {
                        val serverId = abs(entity.id)
                        api.deleteGame(serverId)
                        gameDao.deleteById(entity.id)
                    }
                }
            } catch (e: Exception) {
                gameDao.updateSyncStatus(entity.id, SyncStatus.ERROR)
                hasError = true
            }
        }

        return if (hasError) Result.retry() else Result.success()
    }
}
