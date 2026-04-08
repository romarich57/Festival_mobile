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
 * Rôle : Convertit un DTO réseau en entité Room (SYNCED = données fraîches du serveur).
 * Sérialise notamment la liste des mécanismes au format JSON pour le stockage local.
 * 
 * Précondition : Le DTO `GameDto` reçu du backend doit être complet et valide.
 * Postcondition : Retourne l'entité Room `GameRoomEntity` prête à être sauvegardée dans le cache local.
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
 * Rôle : Convertit un brouillon de jeu saisi hors-ligne en entité Room persistable de manière temporaire.
 * L'identifiant généré est négatif en attendant la validation par le serveur.
 *
 * Précondition : Le brouillon `GameDraft` est valide pour la création ou l'édition locale, avec un `localId` et un état de synchronisation valides.
 * Postcondition : Crée une entité `GameRoomEntity` contenant le brouillon sérialisé (`pendingDraftJson`) afin de reporter la requête réseau.
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

/**
 * Rôle : Décoder la chaîne JSON contenant les mécanismes (stockée localement dans Room).
 *
 * Précondition : La valeur courante `mechanismsJson` de `GameRoomEntity`.
 * Postcondition : Retourne la liste de composants métiers `MechanismOption` ou une liste vide.
 */
private fun GameRoomEntity.parseMechanisms(): List<MechanismOption> =
    if (mechanismsJson.isBlank() || mechanismsJson == "[]") emptyList()
    else ApiJson.instance.decodeFromString(
        ListSerializer(MechanismOption.serializer()),
        mechanismsJson,
    )

/**
 * Rôle : Mappe une entité Room locale complète de type `GameRoomEntity` vers une vue simplifiée (`GameListItem`).
 * 
 * Précondition : L'entité locale doit être initialisée avec un schéma de jeu valide.
 * Postcondition : Retourne `GameListItem`, entité condensée qui alimentera une RecyclerView dans l'UI.
 */
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

/**
 * Rôle : Convertit une entité base de données (`GameRoomEntity`) pour fournir le modèle de domaine détaillé (`GameDetail`).
 *
 * Précondition : La classe entité comprend des paramètres détaillés de jeu et une liste sérialisée pour les sous-composants.
 * Postcondition : Transforme l'ensemble dans une vue de domaine complète à destination des écrans descriptifs.
 */
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
