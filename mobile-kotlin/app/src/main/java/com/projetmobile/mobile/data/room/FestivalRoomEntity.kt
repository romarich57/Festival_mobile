package com.projetmobile.mobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant un festival dans la base de données locale.
 *
 * Les festivals restent pilotés par le serveur, mais la suppression peut être
 * planifiée localement pour la synchronisation offline-first.
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
    val retryAction: String? = null,
    val lastSyncErrorMessage: String? = null,
)
