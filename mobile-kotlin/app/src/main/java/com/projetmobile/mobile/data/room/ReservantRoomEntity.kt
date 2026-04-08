package com.projetmobile.mobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Rôle : Entité Room définissant le schéma de base de données pour un "réservant" (éditeur ou créateur).
 * Elle gère à la fois ses caractéristiques métiers et son état de synchronisation local (Off-line first).
 *
 * Précondition : Utilisée conjointement avec le [com.projetmobile.mobile.data.dao.ReservantDao]
 * et les autres composants de la base locale Room.
 * Postcondition : Autorise la mise en cache réseau locale grâce à ses attributs :
 * Convention des IDs : (id > 0 = ID serveur, id < 0 = ID local temporaire).
 * [pendingDraftJson] : Structure métier sérialisée contenant les modifications de brouillon à remonter.
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
    val retryAction: String? = null,
    val lastSyncErrorMessage: String? = null,
)
