package com.projetmobile.mobile.data.repository.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantContact
import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantDeleteSummary
import com.projetmobile.mobile.data.entity.reservants.ReservantDetail
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantEditorOption
import com.projetmobile.mobile.data.entity.reservants.ReservantListItem

interface ReservantsRepository {
    suspend fun getReservants(): Result<List<ReservantListItem>>

    suspend fun getReservant(reservantId: Int): Result<ReservantDetail>

    suspend fun getEditors(): Result<List<ReservantEditorOption>>

    suspend fun createReservant(draft: ReservantDraft): Result<ReservantDetail>

    suspend fun updateReservant(
        reservantId: Int,
        draft: ReservantDraft,
    ): Result<ReservantDetail>

    suspend fun getDeleteSummary(reservantId: Int): Result<ReservantDeleteSummary>

    suspend fun deleteReservant(reservantId: Int): Result<String>

    suspend fun getContacts(reservantId: Int): Result<List<ReservantContact>>

    suspend fun addContact(
        reservantId: Int,
        draft: ReservantContactDraft,
    ): Result<ReservantContact>
}
