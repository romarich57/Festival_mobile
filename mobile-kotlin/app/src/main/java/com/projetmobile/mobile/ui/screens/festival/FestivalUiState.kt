package com.projetmobile.mobile.ui.screens.festival

import com.projetmobile.mobile.data.entity.festival.FestivalSummary

data class FestivalUiState(
    val isLoading: Boolean = true,
    val festivals: List<FestivalSummary> = emptyList(),
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
