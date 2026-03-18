package com.projetmobile.mobile.ui.screens.auth.register

data class RegisterUiState(
    val username: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val usernameError: String? = null,
    val firstNameError: String? = null,
    val lastNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val pendingVerificationEmail: String? = null,
)
