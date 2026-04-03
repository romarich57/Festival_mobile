package com.projetmobile.mobile.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.projetmobile.mobile.BuildConfig
import com.projetmobile.mobile.data.database.AppDatabase
import com.projetmobile.mobile.data.mapper.reservation.toReservationRoomEntity
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.reservation.ReservationApiService
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.room.SyncStatus
import okhttp3.MediaType.Companion.toMediaType
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
        val db = AppDatabase.getInstance(applicationContext)
        val reservationDao = db.reservationDao()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .addConverterFactory(
                ApiJson.instance.asConverterFactory("application/json".toMediaType()),
            )
            .build()
        val api = retrofit.create(ReservationApiService::class.java)

        val pending = reservationDao.getPending()
        if (pending.isEmpty()) return Result.success()

        var hasError = false

        for (entity in pending) {
            try {
                when (entity.syncStatus) {
                    SyncStatus.PENDING_CREATE -> {
                        val payload = ApiJson.instance.decodeFromString(
                            ReservationCreatePayloadDto.serializer(),
                            entity.pendingDraftJson!!,
                        )
                        api.createReservation(payload)
                        // Rafraîchit les réservations du festival pour remplacer l'item local
                        val serverDtos = api.getReservationsByFestival(entity.festivalId)
                        reservationDao.deleteById(entity.id)
                        reservationDao.upsertAll(
                            serverDtos.map {
                                it.toReservationRoomEntity(entity.festivalId, SyncStatus.SYNCED)
                            },
                        )
                    }

                    SyncStatus.PENDING_DELETE -> {
                        val serverId = abs(entity.id)
                        api.deleteReservation(serverId)
                        reservationDao.deleteById(entity.id)
                    }
                }
            } catch (e: Exception) {
                reservationDao.updateSyncStatus(entity.id, SyncStatus.ERROR)
                hasError = true
            }
        }

        return if (hasError) Result.retry() else Result.success()
    }
}
