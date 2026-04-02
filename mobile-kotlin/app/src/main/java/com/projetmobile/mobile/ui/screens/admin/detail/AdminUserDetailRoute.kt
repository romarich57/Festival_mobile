package com.projetmobile.mobile.ui.screens.admin.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projetmobile.mobile.data.repository.admin.AdminRepository

@Composable
internal fun AdminUserDetailRoute(
    adminRepository: AdminRepository,
    userId: Int,
    onEditUser: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AdminUserDetailViewModel = viewModel(
        factory = AdminUserDetailViewModel.factory(adminRepository, userId),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminUserDetailScreen(
        uiState = uiState,
        onEditUser = { uiState.user?.let { onEditUser(it.id) } },
        modifier = modifier,
    )
}
