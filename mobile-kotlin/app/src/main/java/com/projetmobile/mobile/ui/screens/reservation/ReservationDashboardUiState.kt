package com.projetmobile.mobile.ui.screens.reservation

import com.projetmobile.mobile.data.entity.ReservationDashboardRowEntity

data class ReservationDashboardUiState(
    val isLoading: Boolean = false,
    val reservations: List<ReservationDashboardRowEntity> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val typeFilter: String = "all",
    val sortKey: String = "name-asc",
)
