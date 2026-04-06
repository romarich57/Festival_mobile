package com.projetmobile.mobile.ui.screens.reservationform

import com.projetmobile.mobile.data.entity.reservants.ReservantListItem

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
