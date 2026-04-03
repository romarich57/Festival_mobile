package com.projetmobile.mobile.data.repository.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantContact
import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantEditorOption
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem
import kotlinx.coroutines.flow.Flow

interface ReservantsRepository {

    // ── Observation Room (SSOT) ──────────────────────────────────────────────

    /** Flux live de tous les réservants depuis Room. */
    fun observeReservants(): Flow<List<ReservantListItem>>

    /** Flux live d'un réservant par ID. */
    fun observeReservant(reservantId: Int): Flow<ReservantDetail?>

    // ── Déclenchement réseau ────────────────────────────────────────────────

    /** Rafraîchit les réservants depuis le réseau et met à jour Room. */
    suspend fun refreshReservants(): Result<List<ReservantListItem>>

    suspend fun getReservant(reservantId: Int): Result<ReservantDetail>

    // ── Écriture offline-first ───────────────────────────────────────────────

    suspend fun createReservant(draft: ReservantDraft): Result<ReservantDetail>

    suspend fun updateReservant(
        reservantId: Int,
        draft: ReservantDraft,
    ): Result<ReservantDetail>

    suspend fun getDeleteSummary(reservantId: Int): Result<ReservantDeleteSummary>

    suspend fun deleteReservant(reservantId: Int): Result<String>

    // ── Contacts (réseau direct) ─────────────────────────────────────────────

    suspend fun getEditors(): Result<List<ReservantEditorOption>>

    suspend fun getContacts(reservantId: Int): Result<List<ReservantContact>>

    suspend fun addContact(
        reservantId: Int,
        draft: ReservantContactDraft,
    ): Result<ReservantContact>
}
