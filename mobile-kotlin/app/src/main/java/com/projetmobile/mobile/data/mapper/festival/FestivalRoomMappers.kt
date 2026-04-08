package com.projetmobile.mobile.data.mapper.festival

import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalDto
import com.projetmobile.mobile.data.room.FestivalRoomEntity
import com.projetmobile.mobile.data.room.SyncStatus
import com.projetmobile.mobile.ui.utils.formatDate

// ── DTO réseau → Entité Room ─────────────────────────────────────────────────

/**
 * Stocke les dates au format ISO brut en Room.
 * Le formatage (dd/MM/yyyy) est appliqué lors de la lecture vers le domaine.
 */
fun FestivalDto.toFestivalRoomEntity(
    syncStatus: String = SyncStatus.SYNCED,
    retryAction: String? = null,
    lastSyncErrorMessage: String? = null,
): FestivalRoomEntity = FestivalRoomEntity(
    id = id ?: -1,
    name = name,
    startDate = startDate,
    endDate = endDate,
    stockTablesStandard = stockTablesStandard,
    stockTablesGrande = stockTablesGrande,
    stockTablesMairie = stockTablesMairie,
    stockChaises = stockChaises,
    prixPrises = prixPrises,
    syncStatus = syncStatus,
    retryAction = retryAction,
    lastSyncErrorMessage = lastSyncErrorMessage,
)

// ── Entité Room → Domaine ────────────────────────────────────────────────────

fun FestivalRoomEntity.toFestivalSummary(): FestivalSummary = FestivalSummary(
    id = id,
    name = name,
    startDate = formatDate(startDate),
    endDate = formatDate(endDate),
    stockTablesStandard = stockTablesStandard,
    stockTablesGrande = stockTablesGrande,
    stockTablesMairie = stockTablesMairie,
    stockChaises = stockChaises,
    prixPrises = prixPrises,
)
