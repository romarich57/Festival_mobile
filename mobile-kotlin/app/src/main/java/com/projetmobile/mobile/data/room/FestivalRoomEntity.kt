package com.projetmobile.mobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant un festival dans la base de données locale.
 *
 * Les festivals sont en lecture seule depuis le serveur : pas de pendingDraftJson.
 * [syncStatus] est toujours SYNCED pour les festivals.
 */
@Entity(tableName = "festivals")
data class FestivalRoomEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val startDate: String,
    val endDate: String,
    val stockTablesStandard: Int,
    val stockTablesGrande: Int,
    val stockTablesMairie: Int,
    val stockChaises: Int,
    val prixPrises: Double,
    val syncStatus: String = SyncStatus.SYNCED,
)
