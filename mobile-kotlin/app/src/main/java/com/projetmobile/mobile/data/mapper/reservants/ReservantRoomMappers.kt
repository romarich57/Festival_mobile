package com.projetmobile.mobile.data.mapper.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.reservants.ReservantDto
import com.projetmobile.mobile.data.room.ReservantRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus

// ── DTO réseau → Entité Room ─────────────────────────────────────────────────

/**
 * Rôle : Traduire le DTO réseau Reservant vers son pendant Room persistable.
 * 
 * Précondition : Le `ReservantDto` entrant provient typiquement de l'API. L'état paramétrique initial `syncStatus` est par défaut SYNCED.
 * Postcondition : `ReservantRoomEntity` formaté, prêt à écraser ou insérer la donnée du serveur dans le cache local.
 */
fun ReservantDto.toReservantRoomEntity(
    syncStatus: String = SyncStatus.SYNCED,
    pendingDraftJson: String? = null,
    retryAction: String? = null,
    lastSyncErrorMessage: String? = null,
): ReservantRoomEntity = ReservantRoomEntity(
    id = id,
    name = name,
    email = email,
    type = type,
    editorId = editorId,
    phoneNumber = phoneNumber,
    address = address,
    siret = siret,
    notes = notes,
    syncStatus = syncStatus,
    pendingDraftJson = pendingDraftJson,
    retryAction = retryAction,
    lastSyncErrorMessage = lastSyncErrorMessage,
)

// ── Draft hors-ligne → Entité Room ──────────────────────────────────────────

/**
 * Rôle : Transformer un brouillon de modération de Reservant (Création ou Mise à jour offline) en Entité temporaire locale.
 * Sérialise les données brouillons pour empêcher leur perte en l'absence de connectivité.
 * 
 * Précondition : Un objet `ReservantDraft` contenant les données saisies par l'utilisateur. Un ID fictif local doit être fourni.
 * Postcondition : L'entité Room `ReservantRoomEntity` retient le Draft complet sous format JSON et assigne le `SyncRetryAction` pertinent pour la pile hors-ligne.
 */
fun ReservantDraft.toReservantRoomEntity(
    localId: Int,
    syncStatus: String,
): ReservantRoomEntity = ReservantRoomEntity(
    id = localId,
    name = name,
    email = email,
    type = type,
    editorId = editorId,
    phoneNumber = phoneNumber,
    address = address,
    siret = siret,
    notes = notes,
    syncStatus = syncStatus,
    pendingDraftJson = ApiJson.instance.encodeToString(ReservantDraft.serializer(), this),
    retryAction = when (syncStatus) {
        SyncStatus.PENDING_CREATE -> SyncRetryAction.CREATE
        SyncStatus.PENDING_UPDATE -> SyncRetryAction.UPDATE
        else -> null
    },
    lastSyncErrorMessage = null,
)

// ── Entité Room → Domaine ────────────────────────────────────────────────────

/**
 * Rôle : Préparer l'entité locale (Room) pour affichage résumé (Item de liste UI).
 * 
 * Précondition : Objet `ReservantRoomEntity` mis en cache dans Room.
 * Postcondition : `ReservantListItem` léger et manipulable pour le composant RecyclerView ou List d'Android View.
 */
fun ReservantRoomEntity.toReservantListItem(): ReservantListItem = ReservantListItem(
    id = id,
    name = name,
    email = email,
    type = type,
    editorId = editorId,
    phoneNumber = phoneNumber,
    address = address,
    siret = siret,
    notes = notes,
)

/**
 * Rôle : Alimente de manière exhaustive l'objet Métier à destination de la vue détails d'un exposant / réservant.
 * 
 * Précondition : La classe `ReservantRoomEntity` avec toutes ses colonnes locales actives.
 * Postcondition : Convertit les variables locales vers le modèle applicatif `ReservantDetail`.
 */
fun ReservantRoomEntity.toReservantDetail(): ReservantDetail = ReservantDetail(
    id = id,
    name = name,
    email = email,
    type = type,
    editorId = editorId,
    phoneNumber = phoneNumber,
    address = address,
    siret = siret,
    notes = notes,
)
