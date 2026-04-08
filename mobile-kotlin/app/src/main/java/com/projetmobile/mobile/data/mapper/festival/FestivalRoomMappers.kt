package com.projetmobile.mobile.data.mapper.festival

import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalDto
import com.projetmobile.mobile.data.room.FestivalRoomEntity
import com.projetmobile.mobile.data.room.SyncStatus
import com.projetmobile.mobile.ui.utils.formatDate

// ── DTO réseau → Entité Room ─────────────────────────────────────────────────

/**
 * Rôle : Traduire un DTO envoyé par le réseau vers son format de sauvegarde local (base de données).
 * Les dates au format ISO brut sont stockées telles quelles dans Room.
 * 
 * Précondition : Le DTO entrant contient les informations associées aux festivals provenant de l'API.
 * Postcondition : Convertit les informations en instance `FestivalRoomEntity` destinée à l'insertion locale, avec statut de synchronisation.
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

/**
 * Rôle : Préparer une entité lue depuis le cache local (Room) à être traitée et affichée par la couche supérieure.
 * 
 * Précondition : L'entité festival `FestivalRoomEntity` existe dans la base locale et est bien formatée.
 * Postcondition : Construit et retourne le `FestivalSummary` requis par les écrans.
 */
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
