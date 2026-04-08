package com.projetmobile.mobile.data.mapper.reservation

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ReservationDashboardRowDto
import com.projetmobile.mobile.data.room.ReservationRoomEntity
import com.projetmobile.mobile.data.room.SyncRetryAction
import com.projetmobile.mobile.data.room.SyncStatus

// ── DTO réseau → Entité Room ─────────────────────────────────────────────────

/**
 * Rôle : Persister une ligne du tableau de bord de réservation réseau dans la table locale Room.
 * 
 * Précondition : Le paramètre `festivalId` associé est connu de manière fiable via la vue actuelle. `ReservationDashboardRowDto` est valide.
 * Postcondition : `ReservationRoomEntity` avec `reservantName` transformé en chaîne vide si nul afin d'économiser un null check constant + application des status Sync/Retry.
 */
fun ReservationDashboardRowDto.toReservationRoomEntity(
    festivalId: Int,
    syncStatus: String = SyncStatus.SYNCED,
    pendingDraftJson: String? = null,
    retryAction: String? = null,
    lastSyncErrorMessage: String? = null,
): ReservationRoomEntity = ReservationRoomEntity(
    id = id,
    festivalId = festivalId,
    reservantName = reservantName ?: "",
    reservantType = reservantType,
    workflowState = workflowState,
    syncStatus = syncStatus,
    pendingDraftJson = pendingDraftJson,
    retryAction = retryAction,
    lastSyncErrorMessage = lastSyncErrorMessage,
)

// ── Payload hors-ligne → Entité Room ────────────────────────────────────────

/**
 * Rôle : Traduire une pile d'attente de création de réservation réseau (`PayloadDto`) en une Entité en mode "Draft".
 * 
 * Précondition : `localId` doit être un identificateur négatif géré par cache en Offline-first pour ne pas obstruer l'ID autoincrement DB.
 * Postcondition : Construit puis retourne le `ReservationRoomEntity` contenant le "pending" tag et encode les informations sous JSON (`pendingDraftJson`).
 */
fun ReservationCreatePayloadDto.toReservationRoomEntity(
    localId: Int,
): ReservationRoomEntity = ReservationRoomEntity(
    id = localId,
    festivalId = festivalId,
    reservantName = reservantName,
    reservantType = reservantType,
    workflowState = "pending",
    syncStatus = SyncStatus.PENDING_CREATE,
    pendingDraftJson = ApiJson.instance.encodeToString(
        ReservationCreatePayloadDto.serializer(),
        this,
    ),
    retryAction = SyncRetryAction.CREATE,
    lastSyncErrorMessage = null,
)

// ── Entité Room → Domaine ────────────────────────────────────────────────────

/**
 * Rôle : Décoder un enregistrement SQL local de réservation vers l'entité structurante d'UI du Tableau de bord.
 * 
 * Précondition : Objet SQLite valide sans valeur aberrante null sur les strings essentielles.
 * Postcondition : `ReservationDashboardRowEntity` propice à l'utilisation RecyclerView (sans JSON inutile).
 */
fun ReservationRoomEntity.toReservationDashboardRow(): ReservationDashboardRowEntity =
    ReservationDashboardRowEntity(
        id = id,
        reservantName = reservantName,
        reservantType = reservantType,
        workflowState = workflowState,
    )
