package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.ReservantRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour les réservants.
 */
@Dao
interface ReservantDao {

    /** Flux de tous les réservants non supprimés, triés par nom. */
    @Query(
        """
        SELECT * FROM reservants
        WHERE syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
        ORDER BY name ASC
        """,
    )
    fun observeAll(): Flow<List<ReservantRoomEntity>>

    /** Flux d'un réservant par ID. */
    @Query("SELECT * FROM reservants WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<ReservantRoomEntity?>

    /** Récupère un réservant par ID. */
    @Query("SELECT * FROM reservants WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ReservantRoomEntity?

    /** Tous les items en attente de synchronisation. */
    @Query(
        """
        SELECT * FROM reservants
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun getPending(): List<ReservantRoomEntity>

    @Query("SELECT * FROM reservants")
    suspend fun getAll(): List<ReservantRoomEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM reservants
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun countPendingWork(): Int

    @Upsert
    suspend fun upsertAll(reservants: List<ReservantRoomEntity>)

    @Upsert
    suspend fun upsert(reservant: ReservantRoomEntity)

    @Query("DELETE FROM reservants WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query(
        """
        UPDATE reservants
        SET syncStatus = '${SyncStatus.PENDING_DELETE}',
            retryAction = '${SyncRetryAction.DELETE}',
            lastSyncErrorMessage = NULL
        WHERE id = :id
        """,
    )
    suspend fun markForDeletion(id: Int)

    @Query(
        """
        UPDATE reservants
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
