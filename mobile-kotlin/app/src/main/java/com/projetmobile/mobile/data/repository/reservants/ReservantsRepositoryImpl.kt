package com.projetmobile.mobile.data.repository.reservants

import com.projetmobile.mobile.data.entity.reservants.ReservantContactDraft
import com.projetmobile.mobile.data.entity.reservants.ReservantDraft
import com.projetmobile.mobile.data.mapper.reservants.toReservantContact
import com.projetmobile.mobile.data.mapper.reservants.toReservantDeleteSummary
import com.projetmobile.mobile.data.mapper.reservants.toReservantDetail
import com.projetmobile.mobile.data.mapper.reservants.toReservantEditorOption
import com.projetmobile.mobile.data.mapper.reservants.toReservantListItem
import com.projetmobile.mobile.data.remote.reservants.ReservantsApiService
import com.projetmobile.mobile.data.remote.reservants.toRequestDto
import com.projetmobile.mobile.data.repository.runRepositoryCall

class ReservantsRepositoryImpl(
    private val reservantsApiService: ReservantsApiService,
) : ReservantsRepository {

    override suspend fun getReservants() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les réservants.",
    ) {
        reservantsApiService.getReservants()
            .map { reservant -> reservant.toReservantListItem() }
    }

    override suspend fun getReservant(reservantId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer le réservant.",
    ) {
        reservantsApiService.getReservant(reservantId).toReservantDetail()
    }

    override suspend fun getEditors() = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les éditeurs.",
    ) {
        reservantsApiService.getEditors()
            .map { editor -> editor.toReservantEditorOption() }
    }

    override suspend fun createReservant(draft: ReservantDraft) = runRepositoryCall(
        defaultMessage = "Impossible de créer le réservant.",
    ) {
        reservantsApiService.createReservant(draft.toRequestDto()).toReservantDetail()
    }

    override suspend fun updateReservant(
        reservantId: Int,
        draft: ReservantDraft,
    ) = runRepositoryCall(
        defaultMessage = "Impossible de mettre à jour le réservant.",
    ) {
        reservantsApiService.updateReservant(
            reservantId = reservantId,
            request = draft.toRequestDto(),
        ).toReservantDetail()
    }

    override suspend fun getDeleteSummary(reservantId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de charger le résumé de suppression du réservant.",
    ) {
        reservantsApiService.getDeleteSummary(reservantId).toReservantDeleteSummary()
    }

    override suspend fun deleteReservant(reservantId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de supprimer le réservant.",
    ) {
        reservantsApiService.deleteReservant(reservantId).message
    }

    override suspend fun getContacts(reservantId: Int) = runRepositoryCall(
        defaultMessage = "Impossible de récupérer les contacts du réservant.",
    ) {
        reservantsApiService.getContacts(reservantId)
            .map { contact -> contact.toReservantContact() }
    }

    override suspend fun addContact(
        reservantId: Int,
        draft: ReservantContactDraft,
    ) = runRepositoryCall(
        defaultMessage = "Impossible d'ajouter le contact.",
    ) {
        reservantsApiService.addContact(
            reservantId = reservantId,
            request = draft.toRequestDto(),
        ).toReservantContact()
    }
}
