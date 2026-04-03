package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.projetmobile.mobile.data.room.FestivalRoomEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room pour les festivals (lecture seule côté écriture).
 */
@Dao
interface FestivalDao {

    /** Flux de tous les festivals. */
    @Query("SELECT * FROM festivals ORDER BY startDate DESC")
    fun observeAll(): Flow<List<FestivalRoomEntity>>

    /** Flux d'un festival par ID. */
    @Query("SELECT * FROM festivals WHERE id = :id LIMIT 1")
    fun observeById(id: Int): Flow<FestivalRoomEntity?>

    @Upsert
    suspend fun upsertAll(festivals: List<FestivalRoomEntity>)

    @Upsert
    suspend fun upsert(festival: FestivalRoomEntity)
}
