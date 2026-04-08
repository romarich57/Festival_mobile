package com.projetmobile.mobile.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.projetmobile.mobile.FestivalApplication
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.mapper.reservants.toReservantRoomEntity
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.reservants.toRequestDto
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import com.projetmobile.mobile.data.sync.resolveRetryAction
import kotlin.math.abs

/**
 * Worker chargé de synchroniser les réservants en attente avec le serveur.
 */
class ReservantSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "reservant_sync"
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as FestivalApplication
        val container = app.appContainer
        val db = AppDatabase.getInstance(applicationContext)
        val reservantDao = db.reservantDao()
        val api = container.reservantsApiService

        val pending = reservantDao.getPending()
        if (pending.isEmpty()) return Result.success()

        var hasRetryableError = false

        for (entity in pending) {
            val action = resolveRetryAction(entity.syncStatus, entity.retryAction) ?: continue
            try {
                when (action) {
                    SyncRetryAction.CREATE -> {
                        val draft = ApiJson.instance.decodeFromString(
                            ReservantDraft.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        val serverDto = api.createReservant(draft.toRequestDto())
                        reservantDao.deleteById(entity.id)
                        reservantDao.upsert(serverDto.toReservantRoomEntity(SyncStatus.SYNCED))
                    }

                    SyncRetryAction.UPDATE -> {
                        val draft = ApiJson.instance.decodeFromString(
                            ReservantDraft.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        val serverId = abs(entity.id)
                        val serverDto = api.updateReservant(serverId, draft.toRequestDto())
                        reservantDao.upsert(serverDto.toReservantRoomEntity(SyncStatus.SYNCED))
                    }

                    SyncRetryAction.DELETE -> {
                        if (entity.id > 0) {
                            api.deleteReservant(abs(entity.id))
                        }
                        reservantDao.deleteById(entity.id)
                    }
                }
            } catch (throwable: Throwable) {
                if (action == SyncRetryAction.DELETE && throwable.isDeleteAlreadyApplied()) {
                    reservantDao.deleteById(entity.id)
                    continue
                }

                val retryable = throwable.isRetryableSyncFailure(
                    "Impossible de synchroniser le réservant.",
                )
                reservantDao.updateSyncState(
                    id = entity.id,
                    status = SyncStatus.ERROR,
                    retryAction = if (retryable) action else null,
                    lastSyncErrorMessage = throwable.toSyncFailureMessage(
                        "Impossible de synchroniser le réservant.",
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
