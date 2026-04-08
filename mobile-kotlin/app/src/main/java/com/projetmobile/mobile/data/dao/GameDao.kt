package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.GameRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour les jeux.
 *
 * Les méthodes retournant un [Flow] sont la Source de Vérité Unique (SSOT) :
 * l'UI observe ces flows et se met à jour automatiquement à chaque modification locale.
 */
@Dao
interface GameDao {

    /** Flux de tous les jeux non supprimés, triés par titre. */
    @Query(
        """
        SELECT * FROM games
        WHERE syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
        ORDER BY title ASC
        """,
    )
    fun observeAll(): Flow<List<GameRoomEntity>>

    /** Flux filtré par titre (recherche partielle, insensible à la casse). */
    @Query("""
        SELECT * FROM games
        WHERE syncStatus != '${SyncStatus.PENDING_DELETE}'
          AND NOT (syncStatus = '${SyncStatus.ERROR}' AND retryAction = '${SyncRetryAction.DELETE}')
          AND (:search = '' OR LOWER(title) LIKE '%' || LOWER(:search) || '%')
        ORDER BY title ASC
    """)
    fun observeByTitle(search: String): Flow<List<GameRoomEntity>>

    /** Flux d'un seul jeu par ID (null si absent). */
    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<GameRoomEntity?>

    /** Récupère un jeu par ID (suspend, pour usage dans les workers/repositories). */
    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): GameRoomEntity?

    /** Tous les items en attente de synchronisation (hors SYNCED). */
    @Query(
        """
        SELECT * FROM games
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun getPending(): List<GameRoomEntity>

    @Query("SELECT * FROM games")
    suspend fun getAll(): List<GameRoomEntity>

    @Query(
        """
        SELECT COUNT(*)
        FROM games
        WHERE retryAction IS NOT NULL
           OR syncStatus IN ('${SyncStatus.PENDING_CREATE}', '${SyncStatus.PENDING_UPDATE}', '${SyncStatus.PENDING_DELETE}')
        """,
    )
    suspend fun countPendingWork(): Int

    /** Insère ou met à jour une liste de jeux. */
    @Upsert
    suspend fun upsertAll(games: List<GameRoomEntity>)

    /** Insère ou met à jour un seul jeu. */
    @Upsert
    suspend fun upsert(game: GameRoomEntity)

    /** Supprime un jeu par son ID. */
    @Query("DELETE FROM games WHERE id = :id")
    suspend fun deleteById(id: Int)

    /** Marque un jeu pour suppression (PENDING_DELETE) sans le retirer de la DB. */
    @Query(
        """
        UPDATE games
        SET syncStatus = '${SyncStatus.PENDING_DELETE}',
            retryAction = '${SyncRetryAction.DELETE}',
            lastSyncErrorMessage = NULL
        WHERE id = :id
        """,
    )
    suspend fun markForDeletion(id: Int)

    /** Met à jour l'état de synchro d'un jeu. */
    @Query(
        """
        UPDATE games
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
