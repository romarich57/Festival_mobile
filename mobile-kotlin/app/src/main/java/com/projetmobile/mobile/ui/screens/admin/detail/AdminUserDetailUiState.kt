package com.projetmobile.mobile.ui.screens.admin.detail

import com.projetmobile.mobile.data.entity.auth.AuthUser

data class AdminUserDetailUiState(
    val user: AuthUser? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
)
