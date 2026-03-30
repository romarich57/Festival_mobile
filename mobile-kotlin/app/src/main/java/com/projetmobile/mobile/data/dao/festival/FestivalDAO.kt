package com.projetmobile.mobile.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.projetmobile.mobile.data.entity.festival.FestivalEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) pour les festivals.
 *
 * Expose un Flow<List<FestivalEntity>> : l'UI se met à jour automatiquement
 * dès qu'on insère de nouvelles données depuis l'API → offline-first natif.
 */
@Dao
interface FestivalDao {

    /**
     * Observe la liste complète des festivals en temps réel.
     * Utilisé par FestivalRepositoryImpl comme source de vérité.
     */
    @Query("SELECT * FROM festivals ORDER BY name ASC")
    fun observeAll(): Flow<List<FestivalEntity>>

    /**
     * Remplace toute la liste locale par les données fraîches de l'API.
     * REPLACE = insert ou mise à jour si l'id existe déjà.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun replaceAll(festivals: List<FestivalEntity>)

    /** Nombre de festivals en base — utile pour savoir si le cache est vide. */
    @Query("SELECT COUNT(*) FROM festivals")
    suspend fun count(): Int
}