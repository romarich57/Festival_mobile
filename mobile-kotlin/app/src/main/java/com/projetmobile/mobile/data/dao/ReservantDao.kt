package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.ReservantRoomEntity
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour les réservants.
 */
@Dao
interface ReservantDao {

    /** Flux de tous les réservants non supprimés, triés par nom. */
    @Query("SELECT * FROM reservants WHERE syncStatus != '${SyncStatus.PENDING_DELETE}' ORDER BY name ASC")
    fun observeAll(): Flow<List<ReservantRoomEntity>>

    /** Flux d'un réservant par ID. */
    @Query("SELECT * FROM reservants WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<ReservantRoomEntity?>

    /** Récupère un réservant par ID. */
    @Query("SELECT * FROM reservants WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ReservantRoomEntity?

    /** Tous les items en attente de synchronisation. */
    @Query("SELECT * FROM reservants WHERE syncStatus != '${SyncStatus.SYNCED}'")
    suspend fun getPending(): List<ReservantRoomEntity>

    @Upsert
    suspend fun upsertAll(reservants: List<ReservantRoomEntity>)

    @Upsert
    suspend fun upsert(reservant: ReservantRoomEntity)

    @Query("DELETE FROM reservants WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE reservants SET syncStatus = '${SyncStatus.PENDING_DELETE}' WHERE id = :id")
    suspend fun markForDeletion(id: Int)

    @Query("UPDATE reservants SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Int, status: String)
}
