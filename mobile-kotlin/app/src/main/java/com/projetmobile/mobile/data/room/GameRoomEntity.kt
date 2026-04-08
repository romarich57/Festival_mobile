package com.projetmobile.mobile.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entité Room représentant un jeu dans la base de données locale.
 *
 * Convention des IDs :
 *  - id > 0  : ID serveur (item synchronisé ou en attente de mise à jour / suppression)
 *  - id < 0  : ID local temporaire (item créé hors-ligne, en attente de création serveur)
 *
 * [mechanismsJson] : tableau JSON "[{\"id\":1,\"name\":\"Deck Building\",\"description\":null}]"
 * [pendingDraftJson] : [GameDraft] sérialisé en JSON pour les opérations PENDING_CREATE / PENDING_UPDATE
 */
@Entity(tableName = "games")
data class GameRoomEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val type: String,
    val editorId: Int?,
    val editorName: String?,
    val minAge: Int,
    val authors: String,
    val minPlayers: Int?,
    val maxPlayers: Int?,
    val prototype: Boolean,
    val durationMinutes: Int?,
    val theme: String?,
    val description: String?,
    val imageUrl: String?,
    val rulesVideoUrl: String?,
    val mechanismsJson: String,
    val syncStatus: String = SyncStatus.SYNCED,
    val pendingDraftJson: String? = null,
    val retryAction: String? = null,
    val lastSyncErrorMessage: String? = null,
)
