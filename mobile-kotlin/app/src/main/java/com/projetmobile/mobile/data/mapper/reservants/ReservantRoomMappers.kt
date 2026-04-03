package com.projetmobile.mobile.data.mapper.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.reservants.ReservantDto
import com.projetmobile.mobile.data.room.ReservantRoomEntity
import com.projetmobile.mobile.data.room.SyncStatus

// ── DTO réseau → Entité Room ─────────────────────────────────────────────────

fun ReservantDto.toReservantRoomEntity(
    syncStatus: String = SyncStatus.SYNCED,
    pendingDraftJson: String? = null,
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
)

// ── Draft hors-ligne → Entité Room ──────────────────────────────────────────

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
)

// ── Entité Room → Domaine ────────────────────────────────────────────────────

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
