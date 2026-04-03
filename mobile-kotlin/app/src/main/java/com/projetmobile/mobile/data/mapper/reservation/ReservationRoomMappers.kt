package com.projetmobile.mobile.data.mapper.reservation

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity
import com.projetmobile.mobile.data.remote.common.ApiJson
import com.projetmobile.mobile.data.remote.reservation.ReservationCreatePayloadDto
import com.projetmobile.mobile.data.remote.reservation.ReservationDashboardRowDto
import com.projetmobile.mobile.data.room.ReservationRoomEntity
import com.projetmobile.mobile.data.room.SyncStatus

// ── DTO réseau → Entité Room ─────────────────────────────────────────────────

fun ReservationDashboardRowDto.toReservationRoomEntity(
    festivalId: Int,
    syncStatus: String = SyncStatus.SYNCED,
    pendingDraftJson: String? = null,
): ReservationRoomEntity = ReservationRoomEntity(
    id = id,
    festivalId = festivalId,
    reservantName = reservantName ?: "",
    reservantType = reservantType,
    workflowState = workflowState,
    syncStatus = syncStatus,
    pendingDraftJson = pendingDraftJson,
)

// ── Payload hors-ligne → Entité Room ────────────────────────────────────────

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
)

// ── Entité Room → Domaine ────────────────────────────────────────────────────

fun ReservationRoomEntity.toReservationDashboardRow(): ReservationDashboardRowEntity =
    ReservationDashboardRowEntity(
        id = id,
        reservantName = reservantName,
        reservantType = reservantType,
        workflowState = workflowState,
    )
