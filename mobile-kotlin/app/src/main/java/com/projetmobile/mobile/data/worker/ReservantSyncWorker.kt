package com.projetmobile.mobile.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.projetmobile.mobile.BuildConfig
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.mapper.reservants.toReservantRoomEntity
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.reservants.ReservantsApiService
import com.projetmobile.mobile.data.remote.reservants.toRequestDto
import com.projetmobile.mobile.data.room.SyncStatus
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
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
        val db = AppDatabase.getInstance(applicationContext)
        val reservantDao = db.reservantDao()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(
                ApiJson.instance.asConverterFactory("application/json".toMediaType()),
            )
            .build()
        val api = retrofit.create(ReservantsApiService::class.java)

        val pending = reservantDao.getPending()
        if (pending.isEmpty()) return Result.success()

        var hasError = false

        for (entity in pending) {
            try {
                when (entity.syncStatus) {
                    SyncStatus.PENDING_CREATE -> {
                        val draft = ApiJson.instance.decodeFromString(
                            ReservantDraft.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        val serverDto = api.createReservant(draft.toRequestDto())
                        reservantDao.deleteById(entity.id)
                        reservantDao.upsert(serverDto.toReservantRoomEntity(SyncStatus.SYNCED))
                    }

                    SyncStatus.PENDING_UPDATE -> {
                        val draft = ApiJson.instance.decodeFromString(
                            ReservantDraft.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        val serverId = abs(entity.id)
                        val serverDto = api.updateReservant(serverId, draft.toRequestDto())
                        reservantDao.upsert(serverDto.toReservantRoomEntity(SyncStatus.SYNCED))
                    }

                    SyncStatus.PENDING_DELETE -> {
                        val serverId = abs(entity.id)
                        api.deleteReservant(serverId)
                        reservantDao.deleteById(entity.id)
                    }
                }
            } catch (e: Exception) {
                reservantDao.updateSyncStatus(entity.id, SyncStatus.ERROR)
                hasError = true
            }
        }

        return if (hasError) Result.retry() else Result.success()
    }
}
