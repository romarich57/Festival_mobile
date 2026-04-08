package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.ReservationRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour les réservations.
 */
@Dao
interface ReservationDao {

    /** Flux des réservations d'un festival, non supprimées. */
    @Query("""
        SELECT * FROM reservations
        WHERE festivalId = :festivalId
          AND syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
        ORDER BY reservantName ASC
    """)
    fun observeByFestival(festivalId: Int): Flow<List<ReservationRoomEntity>>

    /** Flux d'une réservation par ID. */
    @Query("SELECT * FROM reservations WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<ReservationRoomEntity?>

    /** Récupère une réservation par ID. */
    @Query("SELECT * FROM reservations WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ReservationRoomEntity?

    /** Tous les items en attente de synchronisation. */
    @Query(
        """
        SELECT * FROM reservations
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun getPending(): List<ReservationRoomEntity>

    @Query("SELECT * FROM reservations WHERE festivalId = :festivalId")
    suspend fun getAllByFestival(festivalId: Int): List<ReservationRoomEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM reservations
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun countPendingWork(): Int

    @Upsert
    suspend fun upsertAll(reservations: List<ReservationRoomEntity>)

    @Upsert
    suspend fun upsert(reservation: ReservationRoomEntity)

    @Query("DELETE FROM reservations WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query(
        """
        UPDATE reservations
        SET syncStatus = '${SyncStatus.PENDING_DELETE}',
            retryAction = '${SyncRetryAction.DELETE}',
            lastSyncErrorMessage = NULL
        WHERE id = :id
        """,
    )
    suspend fun markForDeletion(id: Int)

    @Query(
        """
        UPDATE reservations
        SET syncStatus = :status,
            retryAction = :retryAction,
            lastSyncErrorMessage = :lastSyncErrorMessage
        WHERE id = :id
        """,
    )
    suspend fun updateSyncState(
        id: Int,
        status: String,
        retryAction: String?,
        lastSyncErrorMessage: String?,
    )
}
