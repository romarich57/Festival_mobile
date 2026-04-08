package com.projetmobile.mobile.data.worker

import android.content.Context
import com.projetmobile.mobile.FestivalApplication
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.projetmobile.mobile.BuildConfig
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.database.PersistentCookieJar
import com.projetmobile.mobile.data.mapper.reservation.toReservationRoomEntity
import com.projetmobile.mobile.data.remote.auth.AuthRefreshInterceptor
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.reservation.ReservationApiService
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import com.projetmobile.mobile.data.sync.resolveRetryAction
import com.projetmobile.mobile.data.sync.shouldPreserveLocalDuringRefresh
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.math.abs

/**
 * Worker chargé de synchroniser les réservations en attente avec le serveur.
 */
class ReservationSyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "reservation_sync"
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as FestivalApplication
        val container = app.appContainer
        val reservationDao = AppDatabase.getInstance(applicationContext).reservationDao()
        val api = container.reservationApiService

        val pending = reservationDao.getPending()
        if (pending.isEmpty()) return Result.success()

        var hasRetryableError = false

        for (entity in pending) {
            val action = resolveRetryAction(entity.syncStatus, entity.retryAction) ?: continue
            try {
                when (action) {
                    SyncRetryAction.CREATE -> {
                        val payload = ApiJson.instance.decodeFromString(
                            ReservationCreatePayloadDto.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        api.createReservation(payload)
                        val serverDtos = api.getReservationsByFestival(entity.festivalId)
                        reservationDao.deleteById(entity.id)
                        val localById = reservationDao.getAllByFestival(entity.festivalId)
                            .associateBy { it.id }
                        val remoteIds = mutableSetOf<Int>()
                        val mergedEntities = serverDtos.map { dto ->
                            val remoteEntity = dto.toReservationRoomEntity(
                                entity.festivalId,
                                SyncStatus.SYNCED,
                            )
                            remoteIds += remoteEntity.id
                            val localEntity = localById[remoteEntity.id]
                            if (
                                localEntity != null &&
                                shouldPreserveLocalDuringRefresh(
                                    localEntity.syncStatus,
                                    localEntity.retryAction,
                                )
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

                    SyncRetryAction.DELETE -> {
                        if (entity.id > 0) {
                            api.deleteReservation(abs(entity.id))
                        }
                        reservationDao.deleteById(entity.id)
                    }
                }
            } catch (throwable: Throwable) {
                if (action == SyncRetryAction.DELETE && throwable.isDeleteAlreadyApplied()) {
                    reservationDao.deleteById(entity.id)
                    continue
                }

                val retryable = throwable.isRetryableSyncFailure(
                    "Impossible de synchroniser la réservation.",
                )
                reservationDao.updateSyncState(
                    id = entity.id,
                    status = SyncStatus.ERROR,
                    retryAction = if (retryable) action else null,
                    lastSyncErrorMessage = throwable.toSyncFailureMessage(
                        "Impossible de synchroniser la réservation.",
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
