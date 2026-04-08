package com.projetmobile.mobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant une ligne du tableau de bord des réservations.
 *
 * Convention des IDs : id > 0 = serveur, id < 0 = local temporaire (hors-ligne).
 * [festivalId] : référence au festival parent (pour filtrage local).
 * [pendingDraftJson] : [ReservationCreatePayloadDto] sérialisé pour PENDING_CREATE.
 */
@Entity(tableName = "reservations")
data class ReservationRoomEntity(
    @PrimaryKey val id: Int,
    val festivalId: Int,
    val reservantName: String,
    val reservantType: String,
    val workflowState: String,
    val syncStatus: String = SyncStatus.SYNCED,
    val pendingDraftJson: String? = null,
    val retryAction: String? = null,
    val lastSyncErrorMessage: String? = null,
)
