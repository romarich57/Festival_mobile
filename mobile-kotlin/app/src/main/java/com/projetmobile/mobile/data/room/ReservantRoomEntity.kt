package com.projetmobile.mobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant un réservant dans la base de données locale.
 *
 * Convention des IDs : id > 0 = serveur, id < 0 = local temporaire (hors-ligne).
 * [pendingDraftJson] : [ReservantDraft] sérialisé en JSON pour PENDING_CREATE / PENDING_UPDATE.
 */
@Entity(tableName = "reservants")
data class ReservantRoomEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val type: String,
    val editorId: Int?,
    val phoneNumber: String?,
    val address: String?,
    val siret: String?,
    val notes: String?,
    val syncStatus: String = SyncStatus.SYNCED,
    val pendingDraftJson: String? = null,
)
