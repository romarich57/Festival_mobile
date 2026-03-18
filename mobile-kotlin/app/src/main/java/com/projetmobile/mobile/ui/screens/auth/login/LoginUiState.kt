package com.projetmobile.mobile.ui.screens.auth.login

import com.projetmobile.mobile.data.entity.auth.AuthUser

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val identifierError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val authenticatedUser: AuthUser? = null,
)
