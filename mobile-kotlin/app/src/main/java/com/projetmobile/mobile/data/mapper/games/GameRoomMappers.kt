package com.projetmobile.mobile.data.mapper.games

import com.projetmobile.mobile.data.entity.games.GameDetail
import com.projetmobile.mobile.data.entity.games.GameDraft
import com.projetmobile.mobile.data.entity.games.GameListItem
import com.projetmobile.mobile.data.entity.games.MechanismOption
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.games.GameDto
import com.projetmobile.mobile.data.room.GameRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus
import kotlinx.serialization.builtins.ListSerializer

// ── DTO réseau → Entité Room ─────────────────────────────────────────────────

/**
 * Convertit un DTO réseau en entité Room (SYNCED = données fraîches du serveur).
 */
fun GameDto.toGameRoomEntity(
    syncStatus: String = SyncStatus.SYNCED,
    pendingDraftJson: String? = null,
    retryAction: String? = null,
    lastSyncErrorMessage: String? = null,
): GameRoomEntity = GameRoomEntity(
    id = id,
    title = title,
    type = type,
    editorId = editorId,
    editorName = editorName,
    minAge = minAge,
    authors = authors,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    prototype = prototype,
    durationMinutes = durationMinutes,
    theme = theme,
    description = description,
    imageUrl = imageUrl,
    rulesVideoUrl = rulesVideoUrl,
    mechanismsJson = ApiJson.instance.encodeToString(
        ListSerializer(MechanismOption.serializer()),
        mechanisms.map { MechanismOption(it.id, it.name, it.description) },
    ),
    syncStatus = syncStatus,
    pendingDraftJson = pendingDraftJson,
    retryAction = retryAction,
    lastSyncErrorMessage = lastSyncErrorMessage,
)

// ── Draft hors-ligne → Entité Room ──────────────────────────────────────────

/**
 * Convertit un brouillon hors-ligne en entité Room avec un ID local temporaire.
 *
 * @param localId  ID négatif généré localement (sera remplacé par l'ID serveur après sync).
 * @param syncStatus  Statut de synchronisation (PENDING_CREATE ou PENDING_UPDATE).
 */
fun GameDraft.toGameRoomEntity(localId: Int, syncStatus: String): GameRoomEntity = GameRoomEntity(
    id = localId,
    title = title,
    type = type,
    editorId = editorId,
    editorName = null,
    minAge = minAge ?: 0,
    authors = authors,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    prototype = prototype,
    durationMinutes = durationMinutes,
    theme = theme,
    description = description,
    imageUrl = imageUrl,
    rulesVideoUrl = rulesVideoUrl,
    mechanismsJson = "[]",
    syncStatus = syncStatus,
    pendingDraftJson = ApiJson.instance.encodeToString(GameDraft.serializer(), this),
    retryAction = when (syncStatus) {
        SyncStatus.PENDING_CREATE -> SyncRetryAction.CREATE
        SyncStatus.PENDING_UPDATE -> SyncRetryAction.UPDATE
        else -> null
    },
    lastSyncErrorMessage = null,
)

// ── Entité Room → Domaine ────────────────────────────────────────────────────

private fun GameRoomEntity.parseMechanisms(): List<MechanismOption> =
    if (mechanismsJson.isBlank() || mechanismsJson == "[]") emptyList()
    else ApiJson.instance.decodeFromString(
        ListSerializer(MechanismOption.serializer()),
        mechanismsJson,
    )

fun GameRoomEntity.toGameListItem(): GameListItem = GameListItem(
    id = id,
    title = title,
    type = type,
    editorId = editorId,
    editorName = editorName,
    minAge = minAge,
    authors = authors,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    prototype = prototype,
    durationMinutes = durationMinutes,
    theme = theme,
    description = description,
    imageUrl = imageUrl,
    rulesVideoUrl = rulesVideoUrl,
    mechanisms = parseMechanisms(),
)

fun GameRoomEntity.toGameDetail(): GameDetail = GameDetail(
    id = id,
    title = title,
    type = type,
    editorId = editorId,
    editorName = editorName,
    minAge = minAge,
    authors = authors,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    prototype = prototype,
    durationMinutes = durationMinutes,
    theme = theme,
    description = description,
    imageUrl = imageUrl,
    rulesVideoUrl = rulesVideoUrl,
    mechanisms = parseMechanisms(),
)
