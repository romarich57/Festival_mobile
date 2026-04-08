package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.FestivalRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour les festivals (lecture seule côté écriture).
 */
@Dao
interface FestivalDao {

    /** Flux de tous les festivals. */
    @Query(
        """
        SELECT * FROM festivals
        WHERE syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
        ORDER BY startDate DESC
        """,
    )
    fun observeAll(): Flow<List<FestivalRoomEntity>>

    /** Flux d'un festival par ID. */
    @Query("SELECT * FROM festivals WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<FestivalRoomEntity?>

    @Query("SELECT * FROM festivals WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): FestivalRoomEntity?

    @Query("SELECT * FROM festivals")
    suspend fun getAll(): List<FestivalRoomEntity>

    @Upsert
    suspend fun upsertAll(festivals: List<FestivalRoomEntity>)

    @Upsert
    suspend fun upsert(festival: FestivalRoomEntity)

    @Query(
        """
        SELECT * FROM festivals
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun getPending(): List<FestivalRoomEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM festivals
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun countPendingWork(): Int

    @Query("DELETE FROM festivals WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query(
        """
        UPDATE festivals
        SET syncStatus = '${SyncStatus.PENDING_DELETE}',
            retryAction = '${SyncRetryAction.DELETE}',
            lastSyncErrorMessage = NULL
        WHERE id = :id
        """,
    )
    suspend fun markForDeletion(id: Int)

    @Query(
        """
        UPDATE festivals
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
