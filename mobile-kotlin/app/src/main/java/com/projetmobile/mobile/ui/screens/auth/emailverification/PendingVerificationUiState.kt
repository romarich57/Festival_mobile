package com.projetmobile.mobile.ui.screens.auth.emailverification

data class PendingVerificationUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
