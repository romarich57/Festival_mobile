package com.projetmobile.mobile.ui.screens.auth.resetpassword

data class ResetPasswordUiState(
    val token: String = "",
    val password: String = "",
    val confirmation: String = "",
    val passwordError: String? = null,
    val confirmationError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
) {
    val isTokenMissing: Boolean
        get() = token.isBlank()

    val isSubmitEnabled: Boolean
        get() = !isLoading && !isTokenMissing && successMessage == null
}
