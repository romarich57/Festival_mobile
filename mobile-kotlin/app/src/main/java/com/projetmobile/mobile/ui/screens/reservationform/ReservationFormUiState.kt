package com.projetmobile.mobile.ui.screens.reservationform

import com.projetmobile.mobile.data.entity.reservants.ReservantListItem

/**
 * Rôle : Résume l'état des champs de recherche, sélection existante, et formulaire de création pour initier une réservation complète.
 *
 * Précondition : Un `festivalId` et un ensemble d'options d'auteurs ou d'éditeurs chargés en amont.
 *
 * Postcondition : Informe le fragment d'interface via un flux asynchrone non muté directement par le front-end.
 */
data class ReservationFormUiState(
    val isLoading: Boolean = false,
    val useExistingReservant: Boolean = true,
    val reservantOptions: List<ReservantListItem> = emptyList(),
    val selectedReservantId: Int? = null,
    val nom: String = "",
    val email: String = "",
    val type: String = "editeur",
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)
