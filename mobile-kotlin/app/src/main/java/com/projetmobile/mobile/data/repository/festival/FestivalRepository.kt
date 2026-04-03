package com.projetmobile.mobile.data.repository.festival

import com.projetmobile.mobile.data.entity.festival.FestivalSummary
import com.projetmobile.mobile.data.remote.festival.FestivalDto
import kotlinx.coroutines.flow.Flow

interface FestivalRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    /** Flux live de tous les festivals depuis Room. */
    fun observeFestivals(): Flow<List<FestivalSummary>>

    /** Flux live d'un festival par ID. */
    fun observeFestival(id: Int): Flow<FestivalSummary?>

    // ── Déclenchement réseau ────────────────────────────────────────────────

    /** Rafraîchit les festivals depuis le réseau et met à jour Room. */
    suspend fun refreshFestivals(): Result<List<FestivalSummary>>

    suspend fun getFestival(id: Int): Result<FestivalSummary>

    // ── Admin (réseau direct) ────────────────────────────────────────────────

    suspend fun addFestival(festival: FestivalDto): Result<FestivalDto>

    suspend fun deleteFestival(id: Int): Result<Unit>
}
