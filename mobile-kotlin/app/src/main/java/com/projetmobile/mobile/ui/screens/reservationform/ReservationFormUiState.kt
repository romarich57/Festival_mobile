package com.projetmobile.mobile.ui.screens.reservationform

data class ReservationFormUiState(
    val isLoading: Boolean = false,
    val nom: String = "",
    val email: String = "",
    val type: String = "editeur",
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
)
